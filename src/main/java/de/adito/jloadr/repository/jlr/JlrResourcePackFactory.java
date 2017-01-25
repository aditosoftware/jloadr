package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.api.*;

import javax.annotation.*;
import java.io.IOException;
import java.net.*;

/**
 * @author j.boesl, 25.01.17
 */
public class JlrResourcePackFactory implements IResourcePackFactory
{
  @Nullable
  @Override
  public IResourcePack load(@Nonnull URL pUrl)
  {
    URL url = _getConfigFileUrl(pUrl);
    return url == null ? null : new JlrResourcePack(url);
  }

  private URL _getConfigFileUrl(URL pUrl)
  {
    String path = pUrl.getPath();
    if (path.endsWith(JlrResourcePack.CONFIG_FILE_SUFIX))
      return pUrl;

    if (path.endsWith("/"))
      path = path.substring(0, path.length() -1);
    path += JlrResourcePack.CONFIG_FILE_SUFIX;
    try {
      pUrl = new URL(pUrl.getProtocol(), pUrl.getHost(), pUrl.getPort(), path);
      pUrl.openStream().close(); // check existence
      return pUrl;
    }
    catch (IOException pE) {
      // ignore
    }
    return null;
  }
}
