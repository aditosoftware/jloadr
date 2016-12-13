package de.adito.jloadr.jnlp;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;

import javax.annotation.*;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    return JLoadrUtil.hash(jnlpUrl.toExternalForm()).replaceAll("/", "");
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
      resources = getJnlpUrls().stream()
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
          })
          .collect(Collectors.toMap(JnlpURLResource::getId, Function.identity(), (r1, r2) -> r1, LinkedHashMap::new));
    return resources;
  }

}
