package de.adito.jloadr.api;

import javax.annotation.*;
import java.net.URL;

/**
 * @author j.boesl, 08.09.16
 */
public interface IStartConfig
{

  @Nonnull
  IResourcePack getResourcePack();

  @Nonnull
  String[] getStartCommand();

  @Nullable
  URL getSplashURL();

}
