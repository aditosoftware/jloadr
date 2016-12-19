package de.adito.jloadr.repository.jlr.config;

import de.adito.jloadr.common.*;
import org.w3c.dom.Element;

import java.net.URL;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrEntry
{
  private URL url;
  private String hash;


  public JlrEntry(URL pParentUrl, Element pElement)
  {
    String urlString = XMLUtil.getChildElement(pElement, "url").getTextContent().trim();
    url = UrlUtil.getUrl(pParentUrl, urlString);
    hash = XMLUtil.getChildElement(pElement, "hash").getTextContent().trim();
  }

  public URL getUrl()
  {
    return url;
  }

  public String getHash()
  {
    return hash;
  }
}
