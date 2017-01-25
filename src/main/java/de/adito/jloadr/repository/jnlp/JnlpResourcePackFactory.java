package de.adito.jloadr.repository.jnlp;

import de.adito.jloadr.api.*;

import javax.annotation.*;
import java.net.URL;

/**
 * @author j.boesl, 25.01.17
 */
public class JnlpResourcePackFactory implements IResourcePackFactory
{

  @Nullable
  @Override
  public IResourcePack load(@Nonnull URL pUrl)
  {
    return pUrl.getPath().endsWith(".jnlp") ? new JnlpResourcePack(pUrl) : null;
  }

}
