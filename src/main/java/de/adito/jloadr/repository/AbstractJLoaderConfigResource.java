package de.adito.jloadr.repository;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;

import javax.annotation.*;
import java.io.*;

/**
 * @author j.boesl, 26.01.17
 */
public abstract class AbstractJLoaderConfigResource implements IResource
{
  private byte[] config;

  @Nonnull
  @Override
  public IResourceId getId()
  {
    return JLoaderConfig.CONFIG_ID;
  }

  @Nonnull
  @Override
  public InputStream getInputStream() throws IOException
  {
    return new ByteArrayInputStream(getBinaryConfig());
  }

  @Override
  public long getSize() throws IOException
  {
    return getBinaryConfig().length;
  }

  @Nullable
  @Override
  public String getHash()
  {
    try (ByteArrayInputStream inputStream = new ByteArrayInputStream(getBinaryConfig())) {
      return JLoadrUtil.getHash(inputStream);
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  protected synchronized byte[] getBinaryConfig()
  {
    if (config == null) {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        createConfig().save(outputStream);
        config = outputStream.toByteArray();
      }
      catch (IOException pE) {
        throw new RuntimeException(pE);
      }
    }
    return config;
  }

  protected abstract JLoaderConfig createConfig();

}
