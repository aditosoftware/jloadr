package de.adito.jloadr.api;

import javax.annotation.*;
import java.net.URL;

/**
 * @author j.boesl, 25.01.17
 */
public interface IResourcePackFactory
{

  @Nullable
  IResourcePack load(@Nonnull URL pUrl);

}
