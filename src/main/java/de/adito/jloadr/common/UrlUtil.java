package de.adito.jloadr.common;

import javax.annotation.*;
import java.net.*;

/**
 * @author j.boesl, 19.12.16
 */
public class UrlUtil
{

  private UrlUtil()
  {
  }

  @Nonnull
  public static URL getUrl(@Nullable URL pParentUrl, @Nonnull String pUrlString) throws IllegalArgumentException
  {
    try {
      URI uri = new URI(pUrlString);
      if (uri.isAbsolute())
        return uri.toURL();
      if (pParentUrl == null)
        throw new IllegalArgumentException();

      URI parentUri = pParentUrl.toURI();

      if (!pUrlString.startsWith("/"))
        pUrlString = "/" + pUrlString;

      return parentUri.resolve(parentUri.getPath().endsWith("/") ? pUrlString : ".." + pUrlString).toURL();
    }
    catch (URISyntaxException | MalformedURLException pE) {
      throw new IllegalArgumentException(pE);
    }
  }

}
