package de.adito.jloadr.common;

import de.adito.jloadr.repository.jlr.JlrResourcePackFactory;

import java.net.*;

/**
 * @author j.boesl, 19.12.16
 */
public class UrlUtil
{

  private UrlUtil()
  {
  }

  public static URL getRelative(URL pParentUrl, String pUrlString) throws IllegalArgumentException
  {
    try
    {
      URI uri = new URI(pUrlString);
      if (uri.isAbsolute())
        return uri.toURL();
      if (pParentUrl == null)
        throw new IllegalArgumentException();

      URI parentUri = pParentUrl.toURI();
      return parentUri.resolve(parentUri.getPath().endsWith("/") ? pUrlString : pUrlString).toURL();
    }
    catch (URISyntaxException | MalformedURLException pE)
    {
      throw new IllegalArgumentException(pE);
    }
  }

  public static URL getAtHost(URL pParentUrl, String pUrlString)
  {
    try
    {
      return new URL(pParentUrl.getProtocol(), pParentUrl.getHost(), pParentUrl.getPort(), pUrlString);
    }
    catch (MalformedURLException pE)
    {
      throw new IllegalArgumentException(pE);
    }
  }

  public static String getFolderPathForConfig(String pConfigPath)
  {
    if (!pConfigPath.endsWith(JlrResourcePackFactory.CONFIG_FILE_SUFIX))
      throw new RuntimeException("not a config path");

    return pConfigPath.substring(0, pConfigPath.lastIndexOf(JlrResourcePackFactory.CONFIG_FILE_SUFIX));
  }

}
