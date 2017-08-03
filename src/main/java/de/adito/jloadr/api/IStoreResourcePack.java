package de.adito.jloadr.api;

import java.util.List;

/**
 * @author j.boesl, 05.09.16
 */
public interface IStoreResourcePack extends IResourcePack
{

  IStoreResource createResource(IResourceId pId);

  void removeResource(IResourceId pId);

  @Override
  List<IStoreResource> getResources();

  @Override
  IStoreResource getResource(IResourceId pId);

  void writeConfig();

}
