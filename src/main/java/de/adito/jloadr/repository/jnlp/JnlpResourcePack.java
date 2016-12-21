package de.adito.jloadr.repository.jnlp;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;

import javax.annotation.*;
import java.net.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * @author j.boesl, 05.09.16
 */
class JnlpResourcePack implements IResourcePack
{
  private URL jnlpUrl;
  private Collection<JnlpUrl> jnlpUrls;
  private Map<String, IResource> resources;

  JnlpResourcePack(URL pJnlpUrl)
  {
    jnlpUrl = pJnlpUrl;
  }

  @Nonnull
  @Override
  public String getId()
  {
    return JLoadrUtil.getHash(jnlpUrl.toExternalForm()).replaceAll("/", "");
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
          Stream.of(_getSplashResource()))
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

}
