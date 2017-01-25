package de.adito.jloadr;

import de.adito.jloadr.api.*;

import javax.annotation.*;
import java.net.URL;
import java.util.ServiceLoader;

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

  @Nonnull
  public static IResourcePack get(@Nonnull URL pUrl) throws RuntimeException
  {
    IResourcePack pack = getInstance().load(pUrl);
    if (pack == null)
      throw new RuntimeException("resource not supported: " + pUrl.toExternalForm());
    return pack;
  }

  @Nullable
  @Override
  public IResourcePack load(@Nonnull URL pUrl)
  {
    for (IResourcePackFactory factory : factories) {
      IResourcePack pack = factory.load(pUrl);
      if (pack != null)
        return pack;
    }
    return null;
  }
}
