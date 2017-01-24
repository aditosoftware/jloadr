package de.adito.jloadr.api;

import javax.annotation.*;
import java.util.List;

/**
 * @author j.boesl, 05.09.16
 */
public interface IStoreResourcePack extends IResourcePack
{

  @Nonnull
  IStoreResource createResource(@Nonnull IResourceId pId);

  void removeResource(@Nonnull IResourceId pId);

  @Nonnull
  @Override
  List<IStoreResource> getResources();

  @Nullable
  @Override
  IStoreResource getResource(@Nonnull IResourceId pId);

  void writeConfig();

}
