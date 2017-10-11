package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.api.IResourceId;
import de.adito.jloadr.common.XMLUtil;
import org.w3c.dom.*;

import java.io.*;
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
  private Map<IResourceId, JlrEntry> entryMap;


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

  public synchronized JlrEntry getEntry(IResourceId pId)
  {
    return getEntryMap().get(pId);
  }

  public synchronized void addEntry(JlrEntry pEntry)
  {
    getEntryMap().put(pEntry.getId(), pEntry);
  }

  public synchronized void removeEntry(IResourceId pId)
  {
    getEntryMap().remove(pId);
  }

  public synchronized void loadPack()
  {
    try
    {
      Document document = XMLUtil.loadDocument(packUrl);
      entryMap = XMLUtil.findChildElements(document.getDocumentElement(), "entry").stream()
          .map(JlrEntry::new)
          .collect(Collectors.toMap(JlrEntry::getId, Function.identity()));
    }
    catch (RuntimeException pE)
    {
      entryMap = new HashMap<>();
    }
  }

  public synchronized void writePack()
  {
    XMLUtil.saveDocument(packUrl, this::appendToNode);
  }

  public synchronized String getPack()
  {
    OutputStream os = new OutputStream()
    {
      StringBuilder sbr = new StringBuilder();
      @Override
      public void write(int b) throws IOException
      {
        sbr.append((char)b);
      }

      @Override
      public String toString()
      {
        return sbr.toString();
      }
    };

    XMLUtil.saveDocument(os, this::appendToNode);
    return os.toString();
  }

  protected synchronized Map<IResourceId, JlrEntry> getEntryMap()
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
