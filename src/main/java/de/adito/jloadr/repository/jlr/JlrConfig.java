package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.common.*;
import org.w3c.dom.Document;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrConfig
{
  private URL configURL;
  private final Document document;


  public JlrConfig(URL pConfigURL)
  {
    configURL = pConfigURL;
    document = XMLUtil.loadDocument(pConfigURL);
  }

  public URL getUrl()
  {
    return configURL;
  }

  public List<JlrPack> getPacks()
  {
    return XMLUtil.findChildElements(document.getDocumentElement(), "pack").stream()
        .map(element -> {
          try {
            URL url = UrlUtil.getUrl(configURL, element.getTextContent().trim());
            return new JlrPack(url);
          }
          catch (RuntimeException pE) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

}
