package de.adito.jloadr.repository.mux;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.UrlUtil;

import java.io.IOException;
import java.net.URL;

/**
 * @author j.boesl, 25.01.17
 */
public class MuxResourcePackFactory implements IResourcePackFactory
{
  public static final String CONFIG_FILE_SUFIX = ".mux.xml";
  public static final String DEFAULT_CONFIG_FILE = "default" + CONFIG_FILE_SUFIX;

  @Override
  public IResourcePack load(URL pUrl)
  {
    URL url = _getConfigFileUrl(pUrl);
    return url == null ? null : new MuxResourcePack(url);
  }

  private URL _getConfigFileUrl(URL pUrl)
  {
    String path = pUrl.getPath();
    if (path.endsWith(CONFIG_FILE_SUFIX))
      return pUrl;

    if (!path.endsWith("/"))
      path += "/";
    path += DEFAULT_CONFIG_FILE;
    try {
      pUrl = UrlUtil.getAtHost(pUrl, path);
      pUrl.openStream().close(); // check existence
      return pUrl;
    }
    catch (IOException pE) {
      // ignore
    }
    return null;
  }
}
