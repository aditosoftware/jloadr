package de.adito.jloadr;

import de.adito.jloadr.api.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * @author j.boesl, 05.09.16
 */
public class Loader implements ILoader
{

  @Override
  public void load(IStore pStore, IResourcePack pSource)
  {
    String sourceId = pSource.getId();
    IStoreResourcePack localResourcePack = pStore.containsResourcePack(sourceId) ?
        pStore.getResourcePack(sourceId) : pStore.addResourcePack(sourceId);

    List<? extends IResource> remoteResources = pSource.getResources();
    // clean up
    Set<String> newLocalIdSet = remoteResources.stream()
        .map(resource -> _getLocalId(resource.getId()))
        .collect(Collectors.toSet());
    localResourcePack.getResources().stream()
        .map(IResource::getId)
        .filter(id -> !newLocalIdSet.contains(id))
        .forEach(id -> {
          localResourcePack.removeResource(id);
          //System.out.println("removed: " + id);
        });

    // copy missing
    remoteResources.parallelStream().forEach(resource -> {
      try {
        String localId = _getLocalId(resource.getId());

        IStoreResource localResource = localResourcePack.getResource(localId);
        if (localResource == null) {
          localResource = localResourcePack.createResource(localId);

          _copy(localResource, resource, _isPackGz(resource.getId()));

          //System.out.println("added: " + localResource);
        }
        //else
        //  System.out.println("existed: " + localResource);
      }
      catch (Exception pE) {
        pE.printStackTrace();
      }
    });
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
