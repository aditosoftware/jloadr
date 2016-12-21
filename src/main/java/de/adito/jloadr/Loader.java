package de.adito.jloadr;

import de.adito.jloadr.api.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.*;
import java.util.stream.Collectors;
import java.util.zip.*;

/**
 * @author j.boesl, 05.09.16
 */
public class Loader implements ILoader
{

  @Override
  public void load(@Nonnull IStore pStore, @Nonnull IResourcePack pSource, @Nullable IStateCallback pStateCallback)
  {
    String sourceId = pSource.getId();
    IStoreResourcePack localResourcePack = pStore.containsResourcePack(sourceId) ?
        pStore.getResourcePack(sourceId) : pStore.addResourcePack(sourceId);

    List<? extends IResource> remoteResources = pSource.getResources();

    AtomicInteger loadCount = new AtomicInteger();


    // init state callback
    if (pStateCallback != null) {
      IStoreResource localSplashResource = null;
      IResource remoteSplashResource = pSource.getResource("splash");
      // copy splash
      if (remoteSplashResource != null) {
        localSplashResource = localResourcePack.getResource(remoteSplashResource.getId());
        if (localSplashResource == null) {
          localSplashResource = localResourcePack.createResource("splash");
          _copy(localSplashResource, remoteSplashResource);
        }
      }
      pStateCallback.inited(localSplashResource, remoteResources.size());
    }

    // clean up
    Set<String> newLocalIdSet = remoteResources.stream()
        .map(resource -> _getLocalId(resource.getId()))
        .collect(Collectors.toSet());
    localResourcePack.getResources().stream()
        .map(IResource::getId)
        .filter(id -> !newLocalIdSet.contains(id))
        .forEach(localResourcePack::removeResource);

    // copy missing
    remoteResources.parallelStream().forEach(resource -> {
      try {
        String localId = _getLocalId(resource.getId());

        IStoreResource localResource = localResourcePack.getResource(localId);
        if (localResource == null) {
          localResource = localResourcePack.createResource(localId);

          _copy(localResource, resource);
        }
        if (pStateCallback != null)
          pStateCallback.loaded(loadCount.incrementAndGet());
      }
      catch (Exception pE) {
        pE.printStackTrace();
      }
    });

    if (pStateCallback != null)
      pStateCallback.finished();
  }

  private void _copy(IStoreResource localResource, IResource pRemoteResource)
  {
    try {
      boolean isPackGz = _isPackGz(pRemoteResource.getId());
      if (isPackGz) {
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
             }) {
          Pack200.newUnpacker().unpack(gzipInputStream, jarOutputStream);
        }
      }
      else {
        try (OutputStream out = localResource.getOutputStream(); InputStream in = pRemoteResource.getInputStream()) {
          byte[] buffer = new byte[256 * 1024];
          int len;
          while ((len = in.read(buffer)) != -1)
            out.write(buffer, 0, len);
        }
      }
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  private String _getLocalId(String pRemoteId)
  {
    return _isPackGz(pRemoteId) ? pRemoteId.substring(0, pRemoteId.lastIndexOf(".pack.gz")) : pRemoteId;
  }

  private boolean _isPackGz(String pRemoteId)
  {
    return pRemoteId.endsWith(".pack.gz");
  }

}
