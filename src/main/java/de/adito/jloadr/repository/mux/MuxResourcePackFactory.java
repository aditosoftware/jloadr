package de.adito.jloadr.repository.mux;

import de.adito.jloadr.api.*;

import javax.annotation.*;
import java.net.URL;

/**
 * @author j.boesl, 25.01.17
 */
public class MuxResourcePackFactory implements IResourcePackFactory
{

  @Nullable
  @Override
  public IResourcePack load(@Nonnull URL pUrl)
  {
    return pUrl.getPath().endsWith(MuxResourcePack.CONFIG_FILE_SUFIX) ? new MuxResourcePack(pUrl) : null;
  }

}
