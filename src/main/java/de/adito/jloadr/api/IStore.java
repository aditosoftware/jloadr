package de.adito.jloadr.api;

import javax.annotation.*;
import java.util.*;

/**
 * @author j.boesl, 05.09.16
 */
public interface IStore
{

  @Nonnull
  Set<String> getResourcePackIds();

  boolean containsResourcePack(@Nonnull String pId);

  @Nonnull
  IStoreResourcePack getResourcePack(@Nonnull String pId);

  IStoreResourcePack addResourcePack(@Nonnull String pId);

  void removeResourcePack(@Nonnull String pId);

}
