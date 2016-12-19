package de.adito.jloadr.common;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * @author j.boesl, 19.12.16
 */
public class XMLUtil
{

  private XMLUtil()
  {
  }

  public static Document loadDocument(URL pConfigURL) throws RuntimeException
  {
    try (InputStream in = pConfigURL.openStream()) {
      return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
    }
    catch (SAXException | IOException | ParserConfigurationException pE) {
      throw new RuntimeException(pE);
    }
  }

  public static List<Element> findChildElements(Element pElement, String pTagName)
  {
    List<Element> list = new ArrayList<>();
    NodeList childElements = pElement.getElementsByTagName(pTagName);
    for (int i = 0; i < childElements.getLength(); i++) {
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

  public static String getPath(Node pNode)
  {
    Node parentNode = pNode.getParentNode();
    if (parentNode == null)
      return "/" + pNode.getNodeName();
    return getPath(parentNode) + "/" + pNode.getNodeName();
  }

}
