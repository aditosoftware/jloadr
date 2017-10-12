package de.adito.jloadr.repository.mux;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;
import de.adito.jloadr.repository.*;
import org.w3c.dom.Document;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 25.01.17
 */
public class MuxResourcePack implements IResourcePack
{
  private URL packUrl;
  private Collection<_MuxPack> packs;
  private Map<IResourceId, IResource> resourceMap;

  protected MuxResourcePack(URL pUrl)
  {
    packUrl = pUrl;
    Document document = XMLUtil.loadDocument(packUrl);
    packs = XMLUtil.findChildElements(document.getDocumentElement(), "pack").stream()
        .filter(element -> {
          String os = element.getAttribute("os");
          if (os == null || os.isEmpty())
            return true;

          if (OsUtil.getOsType().toString().equalsIgnoreCase(os))
          {
            String bitness = element.getAttribute("bitness");
            return bitness == null || bitness.isEmpty() || OsUtil.getBitness().toString().equalsIgnoreCase(bitness);
          }
          return false;
        })
        .map(element -> {
          try
          {
            String configPath = element.getTextContent().trim();
            Path relativePath = Paths.get(configPath).getParent();
            URL childPackUrl = UrlUtil.getRelative(packUrl, configPath);
            return new _MuxPack(relativePath, ResourcePackFactory.get(childPackUrl));
          }
          catch (RuntimeException pE)
          {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public String getId()
  {
    return JLoadrUtil.getHash(packUrl.toExternalForm());
  }

  @Override
  public List<? extends IResource> getResources()
  {
    return new ArrayList<>(_getResourceMap().values());
  }

  @Override
  public IResource getResource(IResourceId pId)
  {
    return _getResourceMap().get(pId);
  }

  private Map<IResourceId, IResource> _getResourceMap()
  {
    if (resourceMap == null)
    {
      resourceMap = new HashMap<>();
      _MuxedConfig muxedConfig = new _MuxedConfig(packs);
      resourceMap.put(muxedConfig.getId(), muxedConfig);
      packs.stream()
          .flatMap(pack -> pack.getResourcePack().getResources().stream()
              .map(resource -> new WrappedResource(resource)
              {
                @Override
                public IResourceId getId()
                {
                  return new ResourceId(Paths.get(pack.getRelativePath().toString(), resource.getId().toPath().toString()));
                }
              }))
          .forEach(resource -> resourceMap.putIfAbsent(resource.getId(), resource));
    }
    return resourceMap;
  }

  /**
   * MuxedConfig
   */
  private static class _MuxedConfig extends AbstractJLoaderConfigResource
  {
    private List<IResource> configResources;

    _MuxedConfig(Collection<_MuxPack> pPacks)
    {
      configResources = pPacks.stream()
          .map(pack -> pack.getResourcePack().getResource(JLoaderConfig.CONFIG_ID))
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }

    @Override
    public long getLastModified()
    {
      return configResources.stream()
          .map(resource -> {
            try
            {
              return resource.getLastModified();
            }
            catch (IOException pE)
            {
              throw new RuntimeException(pE);
            }
          })
          .reduce(0L, Math::max);
    }

    @Override
    protected JLoaderConfig createConfig()
    {
      return configResources.stream()
          .map(resource -> {
            JLoaderConfig config = new JLoaderConfig();
            try (InputStream inputStream = resource.getInputStream())
            {
              config.load(inputStream);
              return config;
            }
            catch (IOException pE)
            {
              throw new RuntimeException(pE);
            }
          })
          .reduce(new JLoaderConfig(), (config1, config2) -> {
            if (_isEmptyString(config1.getJavaCmd()))
              config1.setJavaCmd(config2.getJavaCmd());
            if (_isEmptyCollection(config1.getVmParameters()))
              config1.setVmParameters(config2.getVmParameters());
            if (_isEmptyCollection(config1.getClasspath()))
              config1.setClasspath(config2.getClasspath());
            if (_isEmptyString(config1.getMainCls()))
              config1.setMainCls(config2.getMainCls());
            if (_isEmptyCollection(config1.getArguments()))
              config1.setArguments(config2.getArguments());
            return config1;
          });
    }

    private boolean _isEmptyString(String pStr)
    {
      return pStr == null || pStr.isEmpty();
    }

    private boolean _isEmptyCollection(Collection<?> pCollection)
    {
      return pCollection == null || pCollection.isEmpty();
    }
  }

  /**
   * Mux element
   */
  private static class _MuxPack
  {
    private Path relativePath;
    private IResourcePack resourcePack;

    _MuxPack(Path pRelativePath, IResourcePack pResourcePack)
    {
      relativePath = pRelativePath;
      resourcePack = pResourcePack;
    }

    public Path getRelativePath()
    {
      return relativePath;
    }

    public IResourcePack getResourcePack()
    {
      return resourcePack;
    }
  }
}
