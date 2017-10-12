package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.UrlUtil;

import java.net.URL;

/**
 * @author j.boesl, 25.01.17
 */
public class JlrResourcePackFactory implements IResourcePackFactory
{
  public static final String CONFIG_FILE_SUFIX = ".jlr.xml";

  @Override
  public IResourcePack load(URL pUrl)
  {
    String path = pUrl.getPath();
    if (path.endsWith(CONFIG_FILE_SUFIX))
    {
      URL resourcesUrl = UrlUtil.getAtHost(pUrl, path.substring(0, path.lastIndexOf(CONFIG_FILE_SUFIX)) + "/");
      return new JlrResourcePack(pUrl, resourcesUrl);
    }
    return null;
  }

}
