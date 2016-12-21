package de.adito.jloadr.repository.jlr.config;

import de.adito.jloadr.common.XMLUtil;
import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;
import java.nio.file.*;
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
    try {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

      Document doc = docBuilder.newDocument();
      appendToNode(doc);

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      try (OutputStream outputStream = Files.newOutputStream(Paths.get(packUrl.toURI()))) {
        transformer.transform(new DOMSource(doc),
                              new StreamResult(new OutputStreamWriter(outputStream, "UTF-8")));
      }
    }
    catch (TransformerException | ParserConfigurationException | IOException | URISyntaxException pE) {
      throw new RuntimeException(pE);
    }
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
