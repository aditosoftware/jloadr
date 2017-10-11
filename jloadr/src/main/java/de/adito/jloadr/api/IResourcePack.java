package de.adito.jloadr.api;

import java.util.List;

/**
 * @author j.boesl, 05.09.16
 */
public interface IResourcePack
{

  String getId();

  List<? extends IResource> getResources();

  IResource getResource(IResourceId pId);

}
