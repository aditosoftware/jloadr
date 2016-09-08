package de.adito.jloadr.api;

import javax.annotation.*;
import java.util.List;

/**
 * @author j.boesl, 05.09.16
 */
public interface IResourcePack
{

  @Nonnull
  String getId();

  @Nonnull
  List<? extends IResource> getResources();

  @Nullable
  IResource getResource(@Nonnull String pId);

}
