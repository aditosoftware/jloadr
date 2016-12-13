package de.adito.jloadr;

import de.adito.jloadr.api.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author j.boesl, 05.09.16
 */
public class Loader implements ILoader
{

  private ScheduledExecutorService executor;

  public Loader(@Nonnull ScheduledExecutorService pExecutor)
  {
    executor = pExecutor;
  }

  @Override
  public void load(@Nonnull IStore pStore, @Nonnull IResourcePack pSource, @Nullable IStateCallback pStateCallback)
  {
    String sourceId = pSource.getId();
    IStoreResourcePack localResourcePack = pStore.containsResourcePack(sourceId) ?
        pStore.getResourcePack(sourceId) : pStore.addResourcePack(sourceId);

    List<? extends IResource> remoteResources = pSource.getResources();

    if (pStateCallback != null)
      executor.execute(() -> pStateCallback.inited(remoteResources.size()));

    // clean up
    Set<String> newLocalIdSet = remoteResources.stream()
        .map(resource -> _getLocalId(resource.getId()))
        .collect(Collectors.toSet());
    localResourcePack.getResources().stream()
        .map(IResource::getId)
        .filter(id -> !newLocalIdSet.contains(id))
        .forEach(localResourcePack::removeResource);

    AtomicInteger loadCount = new AtomicInteger();

    // copy missing
    remoteResources.parallelStream().forEach(resource -> {
      try {
        String localId = _getLocalId(resource.getId());

        IStoreResource localResource = localResourcePack.getResource(localId);
        if (localResource == null) {
          localResource = localResourcePack.createResource(localId);

          _copy(localResource, resource, _isPackGz(resource.getId()));
        }
        if (pStateCallback != null)
          executor.execute(() -> pStateCallback.loaded(loadCount.incrementAndGet()));
      }
      catch (Exception pE) {
        pE.printStackTrace();
      }
    });

    if (pStateCallback != null)
      executor.execute(pStateCallback::finished);
  }

  private void _copy(IStoreResource localResource, IResource pRemoteResource, boolean pIsPackGz)
  {
    try {
      if (pIsPackGz) {
        try (InputStream inputStream = pRemoteResource.getInputStream();
             GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
             OutputStream outputStream = localResource.getOutputStream();
             JarOutputStream jarOutputStream = new JarOutputStream(outputStream)) {
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
