package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.UrlUtil;

import java.net.URL;

/**
 * @author j.boesl, 25.01.17
 */
public class JlrResourcePackFactory implements IResourcePackFactory
{
  public static final String CONFIG_FILE_SUFFIX = ".jlr.xml";

  @Override
  public IResourcePack load(URL pUrl)
  {
    String path = pUrl.getPath();
    if (path.endsWith(CONFIG_FILE_SUFFIX))
    {
      URL resourcesUrl = UrlUtil.getAtHost(pUrl, UrlUtil.getFolderPathForConfig(path) + "/");
      return new JlrResourcePack(pUrl, resourcesUrl);
    }
    return null;
  }

}
