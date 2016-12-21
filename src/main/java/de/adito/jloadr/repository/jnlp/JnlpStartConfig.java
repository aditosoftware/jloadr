package de.adito.jloadr.repository.jnlp;

import de.adito.jloadr.api.*;

import javax.annotation.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 13.12.16
 */
public class JnlpStartConfig implements IStartConfig
{

  private final JnlpResourcePack jnlpResourcePack;

  public JnlpStartConfig(@Nonnull URL pJnlpUrl)
  {
    jnlpResourcePack = new JnlpResourcePack(pJnlpUrl);
  }

  @Nonnull
  @Override
  public IResourcePack getResourcePack()
  {
    return jnlpResourcePack;
  }

  @Nonnull
  @Override
  public String[] getStartCommand()
  {
    String cp = jnlpResourcePack.getResourcesMap().keySet().stream()
        .map(id -> id.replaceAll("\\.pack\\.gz", ""))
        .collect(Collectors.joining(_getCpDelimiter()));

    Collection<JnlpUrl> jnlpUrls = jnlpResourcePack.getJnlpUrls();
    String main = jnlpUrls.stream()
        .flatMap(jnlpUrl -> jnlpUrl.findChildElementsByPath("application-desc").stream())
        .map(element -> element.getAttribute("main-class"))
        .filter(str -> str != null && !str.isEmpty())
        .findAny().orElseThrow(() -> new RuntimeException("no main class defined"));
    /*String main = _getJnlpUrls().stream()
        .flatMap(JnlpUrl::streamJarJnlpReferences)
        .filter(jnlpRef -> Objects.equals(jnlpRef.getJarElement().getAttribute("main"), "true"))
        .map(jnlpRef -> new JnlpURLResource(jnlpRef).getId())
        .findFirst().orElseThrow(() -> new RuntimeException("no main defined"));*/
    List<String> args = jnlpUrls.stream()
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
