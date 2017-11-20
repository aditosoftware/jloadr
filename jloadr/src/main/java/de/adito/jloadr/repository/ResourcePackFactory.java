package de.adito.jloadr.repository;

import de.adito.jloadr.api.*;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author j.boesl, 25.01.17
 */
public class ResourcePackFactory implements IResourcePackFactory
{
  private static final IResourcePackFactory INSTANCE = new ResourcePackFactory();

  private ServiceLoader<IResourcePackFactory> factories;

  protected ResourcePackFactory()
  {
    factories = ServiceLoader.load(IResourcePackFactory.class);
  }

  public static IResourcePackFactory getInstance()
  {
    return INSTANCE;
  }

  public static IResourcePack get(URL pUrl) throws RuntimeException
  {
    IResourcePack pack = getInstance().load(pUrl);
    if (pack == null)
    {
      try (InputStream inputStream = pUrl.openConnection().getInputStream())
      {
        inputStream.read();
      }
      catch (IOException pE)
      {
        throw new RuntimeException(pE);
      }
      throw new RuntimeException("resource not supported: " + pUrl.toExternalForm());
    }
    return pack;
  }

  @Override
  public IResourcePack load(URL pUrl)
  {
    for (IResourcePackFactory factory : factories)
    {
      IResourcePack pack = factory.load(pUrl);
      if (pack != null)
        return pack;
    }
    return null;
  }
}
