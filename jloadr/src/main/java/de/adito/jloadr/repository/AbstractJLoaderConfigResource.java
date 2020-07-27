package de.adito.jloadr.repository;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;

import java.io.*;

/**
 * @author j.boesl, 26.01.17
 */
public abstract class AbstractJLoaderConfigResource implements IResource
{
  private byte[] config;

  @Override
  public IResourceId getId()
  {
    return JLoaderConfig.CONFIG_ID;
  }

  @Override
  public InputStream getInputStream()
  {
    return new ByteArrayInputStream(getBinaryConfig());
  }

  @Override
  public long getSize()
  {
    return getBinaryConfig().length;
  }

  @Override
  public String getHash()
  {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(getBinaryConfig()))
    {
      return JLoadrUtil.getHash(inputStream);
    }
    catch (IOException pE)
    {
      throw new RuntimeException("hash of the resource could not be fetched.", pE);
    }
  }

  protected synchronized byte[] getBinaryConfig()
  {
    if (config == null)
    {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
      {
        createConfig().saveTagsAsXml(outputStream);
        config = outputStream.toByteArray();
      }
      catch (IOException pE)
      {
        throw new RuntimeException("binary config could not be created", pE);
      }
    }
    return config;
  }

  protected abstract JLoaderConfig createConfig();

}
