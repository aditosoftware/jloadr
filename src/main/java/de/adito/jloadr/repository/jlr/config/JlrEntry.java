package de.adito.jloadr.repository.jlr.config;

import de.adito.jloadr.common.XMLUtil;
import org.w3c.dom.*;

import java.net.*;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrEntry
{
  private URL url;
  private String hash;


  public JlrEntry(Element pElement)
  {
    try {
      url = new URL(XMLUtil.getChildElement(pElement, "url").getTextContent().trim());
    }
    catch (MalformedURLException pE) {
      throw new IllegalStateException(pE);
    }
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
