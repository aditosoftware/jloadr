package de.adito.jloadr.repository.jnlp;

import de.adito.jloadr.JLoaderConfig;
import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;

import javax.annotation.*;
import java.io.*;
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
  private Map<String, IResource> resources;

  public JnlpResourcePack(URL pJnlpUrl)
  {
    jnlpUrl = pJnlpUrl;
  }

  @Nonnull
  @Override
  public String getId()
  {
    return JLoadrUtil.normalizeId(JLoadrUtil.getHash(jnlpUrl.toExternalForm()));
  }

  @Nonnull
  @Override
  public List<IResource> getResources()
  {
    return new ArrayList<>(getResourcesMap().values());
  }

  @Nullable
  @Override
  public IResource getResource(@Nonnull String pId)
  {
    return getResourcesMap().get(pId);
  }

  synchronized Collection<JnlpUrl> getJnlpUrls()
  {
    if (jnlpUrls == null)
      jnlpUrls = JnlpUrl.load(jnlpUrl);
    return jnlpUrls;
  }

  synchronized Map<String, IResource> getResourcesMap()
  {
    if (resources == null)
      resources = Stream.concat(
          getJnlpUrls().stream()
              .flatMap(JnlpUrl::streamJarJnlpReferences)
              .map(JnlpURLResource::new)
              .filter(jnlpURLResource -> {
                try {
                  jnlpURLResource.getId();
                  return true;
                }
                catch (Exception pE) {
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
    return getJnlpUrls().stream()
        .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("information/icon").stream()
            .map(element -> new AbstractMap.SimpleImmutableEntry<>(jnlpUrl, element)))
        .filter(entry -> entry.getValue().getAttribute("kind").equals("splash"))
        .map(entry -> {
          try {
            return new URLResource(new URL(entry.getKey().getCodebase(), entry.getValue().getAttribute("href")))
            {
              @Nonnull
              @Override
              public String getId()
              {
                return "splash";
              }
            };
          }
          catch (MalformedURLException pE) {
            return null;
          }
        })
        .findAny().orElse(null);
  }


  private class _ConfigResource implements IResource
  {
    private byte[] config;

    @Nonnull
    @Override
    public String getId()
    {
      return JLoaderConfig.CONFIG_NAME;
    }

    @Nonnull
    @Override
    public InputStream getInputStream() throws IOException
    {
      return new ByteArrayInputStream(_getBinaryConfig());
    }

    @Override
    public long getSize() throws IOException
    {
      return _getBinaryConfig().length;
    }

    @Override
    public long getLastModified() throws IOException
    {
      URLConnection urlConnection = jnlpUrl.openConnection();
      return urlConnection.getLastModified();
    }

    @Nullable
    @Override
    public String getHash()
    {
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(_getBinaryConfig())) {
        return JLoadrUtil.getHash(inputStream);
      }
      catch (IOException pE) {
        throw new RuntimeException(pE);
      }
    }

    private synchronized byte[] _getBinaryConfig()
    {
      if (config == null) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
          _createConfig().save(outputStream);
          config = outputStream.toByteArray();
        }
        catch (IOException pE) {
          throw new RuntimeException(pE);
        }
      }
      return config;
    }

    private JLoaderConfig _createConfig()
    {
      Collection<JnlpUrl> jnlpUrls = getJnlpUrls();

      List<String> vmProperties = jnlpUrls.stream()
          .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("resources/property").stream())
          .map(element -> {
            String name = element.getAttribute("name").replace("jnlp.adito.", "");
            String value = element.getAttribute("value");
            return name.isEmpty() ? null : name + (value.isEmpty() ? "" : "=" + value);
          })
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      List<String> classpath = getResourcesMap().keySet().stream()
          .map(id -> id.replace(".pack.gz", ""))
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
