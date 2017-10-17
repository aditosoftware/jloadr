package de.adito.jloadr.repository.jnlp;

import de.adito.jloadr.Loader;
import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;
import de.adito.jloadr.repository.*;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * @author j.boesl, 05.09.16
 */
public class JnlpResourcePack implements IResourcePack
{
  private URL jnlpUrl;
  private Collection<JnlpUrl> jnlpUrls;
  private Map<IResourceId, IResource> resources;

  JnlpResourcePack(URL pJnlpUrl)
  {
    jnlpUrl = pJnlpUrl;
  }

  @Override
  public String getId()
  {
    return JLoadrUtil.getHash(jnlpUrl.toExternalForm());
  }

  @Override
  public List<IResource> getResources()
  {
    return new ArrayList<>(_getResourcesMap().values());
  }

  @Override
  public IResource getResource(IResourceId pId)
  {
    return _getResourcesMap().get(pId);
  }

  private synchronized Collection<JnlpUrl> _getJnlpUrls()
  {
    if (jnlpUrls == null)
      jnlpUrls = JnlpUrl.load(jnlpUrl);
    return jnlpUrls;
  }

  private synchronized Map<IResourceId, IResource> _getResourcesMap()
  {
    if (resources == null)
      resources = Stream.concat(
          _getJnlpUrls().stream()
              .flatMap(JnlpUrl::streamJarJnlpReferences)
              .map(JnlpURLResource::new)
              .filter(jnlpURLResource -> {
                try
                {
                  jnlpURLResource.getId();
                  return true;
                }
                catch (Exception pE)
                {
                  System.err.println(pE.getMessage());
                  return false;
                }
              }),
          Stream.of(_getSplashResource(), new _ConfigResource()))
          .filter(Objects::nonNull)
          .collect(Collectors.toMap(IResource::getId, Function.identity(), (r1, r2) -> r1, LinkedHashMap::new));
    return resources;
  }

  private IResource _getSplashResource()
  {
    return _getJnlpUrls().stream()
        .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("information/icon").stream()
            .map(element -> new AbstractMap.SimpleImmutableEntry<>(jnlpUrl, element)))
        .filter(entry -> entry.getValue().getAttribute("kind").equals(Loader.SPLASH_ID.toString()))
        .map(entry -> {
          try
          {
            return new URLResource(new URL(entry.getKey().getCodebase(), entry.getValue().getAttribute("href")))
            {
              @Override
              public IResourceId getId()
              {
                return Loader.SPLASH_ID;
              }
            };
          }
          catch (MalformedURLException pE)
          {
            return null;
          }
        })
        .findAny().orElse(null);
  }


  private class _ConfigResource extends AbstractJLoaderConfigResource
  {
    @Override
    public long getLastModified() throws IOException
    {
      return jnlpUrls.stream()
          .map(JnlpUrl::getLastModified)
          .reduce(0L, Math::max);
    }

    @Override
    protected JLoaderConfig createConfig()
    {
      Collection<JnlpUrl> jnlpUrls = _getJnlpUrls();

      List<String> vmProperties = jnlpUrls.stream()
          .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("resources/property").stream())
          .map(element -> {
            String name = element.getAttribute("name").replace("jnlp.adito.", "adito.");
            String value = element.getAttribute("value");
            return name.isEmpty() ? null : name + (value.isEmpty() ? "" : "=" + value);
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      List<String> classpath = _getResourcesMap().keySet().stream()
          .map(id -> id.toString().replace(".pack.gz", ""))
          .collect(Collectors.toList());

      String mainClass = jnlpUrls.stream()
          .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("application-desc").stream())
          .map(element -> element.getAttribute("main-class"))
          .filter(str -> str != null && !str.isEmpty())
          .findAny().orElseThrow(() -> new RuntimeException("no main class defined"));

      List<String> arguments = jnlpUrls.stream()
          .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("application-desc/argument").stream())
          .map(element -> element.getTextContent().trim())
          .collect(Collectors.toList());

      JLoaderConfig jLoaderConfig = new JLoaderConfig();
      jLoaderConfig.setVmParameters(vmProperties);
      jLoaderConfig.setClasspath(classpath);
      jLoaderConfig.setMainCls(mainClass);
      jLoaderConfig.setArguments(arguments);

      return jLoaderConfig;
    }
  }


}
