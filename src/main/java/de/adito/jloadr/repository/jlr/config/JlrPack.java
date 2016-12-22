package de.adito.jloadr.repository.jlr.config;

import de.adito.jloadr.common.XMLUtil;
import org.w3c.dom.*;

import java.net.URL;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrPack
{
  private URL packUrl;
  private Map<String, JlrEntry> entryMap;


  public JlrPack(URL pPackUrl)
  {
    packUrl = pPackUrl;
  }

  public URL getUrl()
  {
    return packUrl;
  }

  public synchronized List<JlrEntry> getEntries()
  {
    return new ArrayList<>(getEntryMap().values());
  }

  public synchronized JlrEntry getEntry(String pId)
  {
    return getEntryMap().get(pId);
  }

  public synchronized void addEntry(JlrEntry pEntry)
  {
    getEntryMap().put(pEntry.getId(), pEntry);
  }

  public synchronized void removeEntry(String pId)
  {
    getEntryMap().remove(pId);
  }

  public synchronized void loadPack()
  {
    try {
      Document document = XMLUtil.loadDocument(packUrl);
      entryMap = XMLUtil.findChildElements(document.getDocumentElement(), "entry").stream()
          .map(JlrEntry::new)
          .collect(Collectors.toMap(JlrEntry::getId, Function.identity()));
    }
    catch (RuntimeException pE) {
      entryMap = new HashMap<>();
    }
  }

  public synchronized void writePack()
  {
    XMLUtil.saveDocument(packUrl, this::appendToNode);
  }

  protected synchronized Map<String, JlrEntry> getEntryMap()
  {
    if (entryMap == null)
      entryMap = new HashMap<>();
    return entryMap;
  }

  protected void appendToNode(Node pNode)
  {
    Document doc = pNode instanceof Document ? (Document) pNode : pNode.getOwnerDocument();
    Element jlrPacks = doc.createElement("jlrPacks");
    pNode.appendChild(jlrPacks);

    for (JlrEntry jlrEntry : getEntryMap().values())
      jlrEntry.appendToNode(jlrPacks);
  }

}
