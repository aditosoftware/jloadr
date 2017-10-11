package de.adito.jloadr.repository.jnlp;

import de.adito.jloadr.api.*;

import java.net.URL;

/**
 * @author j.boesl, 25.01.17
 */
public class JnlpResourcePackFactory implements IResourcePackFactory
{

  @Override
  public IResourcePack load(URL pUrl)
  {
    return pUrl.getPath().endsWith(".jnlp") ? new JnlpResourcePack(pUrl) : null;
  }

}
