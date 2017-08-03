package de.adito.jloadr.common;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author j.boesl, 19.12.16
 */
public class XMLUtil
{

  private XMLUtil()
  {
  }

  public static Document loadDocument(URL pDocumentUrl) throws RuntimeException
  {
    try
    {
      URLConnection urlConnection = pDocumentUrl.openConnection();
      if (urlConnection.getContentLengthLong() == 0)
        throw new RuntimeException();
      try (InputStream in = urlConnection.getInputStream())
      {
        return loadDocument(in);
      }
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  public static Document loadDocument(InputStream pInputStream) throws RuntimeException
  {
    try
    {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pInputStream);
    }
    catch (SAXException | IOException | ParserConfigurationException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  public static void saveDocument(URL pDocumentUrl, Consumer<Document> pAppender)
  {
    try (OutputStream outputStream = Files.newOutputStream(Paths.get(pDocumentUrl.toURI())))
    {
      saveDocument(outputStream, pAppender);
    }
    catch (URISyntaxException | IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  public static void saveDocument(OutputStream pOutputStream, Consumer<Document> pAppender)
  {
    try
    {
      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

      Document doc = docBuilder.newDocument();
      pAppender.accept(doc);

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

      transformer.transform(new DOMSource(doc),
                            new StreamResult(new OutputStreamWriter(pOutputStream, "UTF-8")));
    }
    catch (TransformerException | ParserConfigurationException | IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  public static List<Element> findChildElements(Element pElement, String pTagName)
  {
    List<Element> list = new ArrayList<>();
    NodeList childElements = pElement.getElementsByTagName(pTagName);
    for (int i = 0; i < childElements.getLength(); i++)
    {
      Node item = childElements.item(i);
      list.add((Element) item);
    }
    return list;
  }

  public static Element getChildElement(Element pElement, String pTagName)
  {
    NodeList childElements = pElement.getElementsByTagName(pTagName);
    if (childElements.getLength() == 0)
      throw new IllegalStateException("Tag '" + pTagName + "' could not be found at '" + getPath(pElement) + "'.");
    if (childElements.getLength() > 1)
      throw new IllegalStateException("Too many tags with name '" + pTagName + "' were found at '" + getPath(pElement) + "'.");
    return (Element) childElements.item(0);
  }

  public static String getChildText(Element pElement, String pTagName)
  {
    try
    {
      return getChildElement(pElement, pTagName).getTextContent().trim();
    }
    catch (IllegalStateException pE)
    {
      return null;
    }
  }

  public static String getPath(Node pNode)
  {
    Node parentNode = pNode.getParentNode();
    if (parentNode == null)
      return "/" + pNode.getNodeName();
    return getPath(parentNode) + "/" + pNode.getNodeName();
  }

}
