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
public class JnlpResourcePack implements IResourcePack, IStartConfig
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
    return JLoadrUtil.hash(jnlpUrl.toExternalForm()).replaceAll("/", "");
  }

  @Nonnull
  @Override
  public List<IResource> getResources()
  {
    return new ArrayList<>(_getResources().values());
  }

  @Nullable
  @Override
  public IResource getResource(@Nonnull String pId)
  {
    return _getResources().get(pId);
  }

  private synchronized Collection<JnlpUrl> _getJnlpUrls()
  {
    if (jnlpUrls == null)
      jnlpUrls = JnlpUrl.load(jnlpUrl);
    return jnlpUrls;
  }

  private synchronized Map<String, IResource> _getResources()
  {
    if (resources == null)
      resources = _getJnlpUrls().stream()
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

  @Override
  public String[] getStartCommand()
  {
    String cp = _getResources().keySet().stream()
        .map(id -> id.replaceAll("\\.pack\\.gz", ""))
        .collect(Collectors.joining(_getCpDelimiter()));
    String main = _getJnlpUrls().stream()
        .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("application-desc").stream())
        .map(element -> element.getAttribute("main-class"))
        .filter(str -> str != null && !str.isEmpty())
        .findAny().orElseThrow(() -> new RuntimeException("no main class defined"));
    /*String main = _getJnlpUrls().stream()
        .flatMap(JnlpUrl::streamJarJnlpReferences)
        .filter(jnlpRef -> Objects.equals(jnlpRef.getJarElement().getAttribute("main"), "true"))
        .map(jnlpRef -> new JnlpURLResource(jnlpRef).getId())
        .findFirst().orElseThrow(() -> new RuntimeException("no main defined"));*/
    List<String> args = _getJnlpUrls().stream()
        .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("application-desc/argument").stream())
        .map(element -> element.getTextContent().trim())
        .collect(Collectors.toList());

    List<String> parameters = new ArrayList<>();
    parameters.add("java");
    parameters.add("-cp");
    parameters.add("\"" + cp + "\"");
    parameters.add(main);
    parameters.addAll(args);
    return parameters.toArray(new String[parameters.size()]);
  }

  private String _getCpDelimiter()
  {
    String osName = System.getProperty("os.name");
    return osName != null && osName.startsWith("Windows") ? ";" : ":";
  }
}
