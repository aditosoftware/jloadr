package de.adito.jloadr.repository.jlr.config;

import de.adito.jloadr.common.XMLUtil;
import org.w3c.dom.*;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrPack
{
  private URL packUrl;
  private Document document;


  public JlrPack(URL pPackUrl)
  {
    packUrl = pPackUrl;
    document = XMLUtil.loadDocument(pPackUrl);
  }

  public URL getUrl()
  {
    return packUrl;
  }

  public List<JlrEntry> getEntries()
  {
    return XMLUtil.findChildElements(document.getDocumentElement(), "entry").stream()
        .map(JlrEntry::new)
        .collect(Collectors.toList());
  }

}
