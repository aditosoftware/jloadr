package de.adito.jloadr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;
import de.adito.jloadr.repository.ResourceId;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.jar.*;
import java.util.stream.Collectors;
import java.util.zip.*;

/**
 * @author j.boesl, 05.09.16
 */
public class Loader implements ILoader
{

  public static final Predicate<IResource> FILTER_IGNORE_RESOURCE_PREDICATE =
      resource -> !resource.getId().toPath().subpath(0, 1).toString().startsWith(".");
  public static final ResourceId SPLASH_ID = new ResourceId("splash");

  @Override
  public IStoreResourcePack load(IStore pStore, IResourcePack pSource, IStateCallback pStateCallback)
  {
    String sourceId = pSource.getId();
    IStoreResourcePack localResourcePack = pStore.containsResourcePack(sourceId) ?
        pStore.getResourcePack(sourceId) : pStore.addResourcePack(sourceId);

    List<? extends IResource> remoteResources = pSource.getResources();

    AtomicInteger loadCount = new AtomicInteger();


    // init state callback
    if (pStateCallback != null)
    {
      IStoreResource localSplashResource = null;
      IResource remoteSplashResource = pSource.getResource(SPLASH_ID);
      // copy splash
      if (remoteSplashResource != null)
      {
        localSplashResource = localResourcePack.getResource(remoteSplashResource.getId());
        if (localSplashResource == null)
        {
          localSplashResource = localResourcePack.createResource(SPLASH_ID);
          _copy(localSplashResource, remoteSplashResource);
        }
      }
      pStateCallback.inited(localSplashResource, remoteResources.size());
    }

    // clean up
    Set<IResourceId> newLocalIdSet = remoteResources.stream()
        .map(resource -> _getLocalId(resource.getId()))
        .collect(Collectors.toSet());
    localResourcePack.getResources().stream()
        .filter(FILTER_IGNORE_RESOURCE_PREDICATE)
        .map(IResource::getId)
        .filter(id -> !newLocalIdSet.contains(id))
        .forEach(localResourcePack::removeResource);

    // copy missing
    remoteResources.parallelStream()
        .filter(FILTER_IGNORE_RESOURCE_PREDICATE)
        .forEach(resource -> {
          try
          {
            IResourceId localId = _getLocalId(resource.getId());
            IStoreResource localResource = localResourcePack.getResource(localId);

            if (localResource == null)
              localResource = localResourcePack.createResource(localId);

            if (!localResource.getHash().equals(resource.getHash()))
            {
              _copy(localResource, resource);
            }

            if (pStateCallback != null)
              pStateCallback.loaded(loadCount.incrementAndGet());
          }
          catch (Exception pE)
          {
            System.err.println("error loading: " + resource.getId());
            pE.printStackTrace();
          }
        });

    if (pStateCallback != null)
      pStateCallback.finished();

    localResourcePack.writeConfig();
    return localResourcePack;
  }

  private void _copy(IStoreResource localResource, IResource pRemoteResource)
  {
    try
    {
      boolean isPackGz = _isPackGz(pRemoteResource.getId());
      if (isPackGz)
      {
        try (InputStream inputStream = pRemoteResource.getInputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
             OutputStream outputStream = localResource.getOutputStream();
             JarOutputStream jarOutputStream = new JarOutputStream(outputStream)
             {
               @Override
               public void putNextEntry(ZipEntry ze) throws IOException
               {
                 super.putNextEntry(new ZipEntry(ze.getName()));
               }
             })
        {
          Pack200.newUnpacker().unpack(gzipInputStream, jarOutputStream);
        }
      }
      else
        JLoadrUtil.copy(pRemoteResource.getInputStream(), localResource.getOutputStream());

      localResource.setLastModified(pRemoteResource.getLastModified());
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  private IResourceId _getLocalId(IResourceId pRemoteId)
  {
    String idStr = pRemoteId.toString();
    return _isPackGz(pRemoteId) ? new ResourceId(idStr.substring(0, idStr.lastIndexOf(".pack.gz"))) : pRemoteId;
  }

  private boolean _isPackGz(IResourceId pRemoteId)
  {
    return pRemoteId.toString().endsWith(".pack.gz");
  }

}
