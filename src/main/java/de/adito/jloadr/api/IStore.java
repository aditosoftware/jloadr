package de.adito.jloadr.api;

import java.util.*;

/**
 * @author j.boesl, 05.09.16
 */
public interface IStore
{

  Set<String> getResourcePackIds();

  boolean containsResourcePack(String pId);

  IStoreResourcePack getResourcePack( String pId);

  IStoreResourcePack addResourcePack( String pId);

  void removeResourcePack( String pId);

}
