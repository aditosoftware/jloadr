package de.adito.jloadr.repository.jnlp;

import de.adito.jloadr.common.XMLUtil;
import org.w3c.dom.*;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author j.boesl, 05.09.16
 */
class JnlpUrl
{

  private URL jnlpUrl;
  private Document document;
  private URL codebase;
  private List<Element> jars;
  private List<Element> extensions;


  static Collection<JnlpUrl> load(URL pJnlpUrl)
  {
    Map<String, JnlpUrl> jnlpUrlMap = new LinkedHashMap<>();
    _load(jnlpUrlMap, pJnlpUrl);
    return jnlpUrlMap.values();
  }

  private static void _load(Map<String, JnlpUrl> pMap, URL pJnlpUrl)
  {
    if (pMap.containsKey(pJnlpUrl.toExternalForm()))
      return;
    JnlpUrl jnlpUrl = new JnlpUrl(pJnlpUrl);
    pMap.put(pJnlpUrl.toExternalForm(), jnlpUrl);
    jnlpUrl._streamExtensionJnlpReferences().forEach(jnlpRef -> _load(pMap, jnlpRef.getUrl()));
  }


  private JnlpUrl(URL pJnlpUrl)
  {
    jnlpUrl = pJnlpUrl;
    document = XMLUtil.loadDocument(pJnlpUrl);

    String codebaseString = document.getDocumentElement().getAttribute("codebase");
    if (codebaseString != null && !codebaseString.isEmpty())
    {
      try
      {
        codebase = new URL(codebaseString);
      }
      catch (MalformedURLException pE)
      {
        // alternativ wird codebase aus 'jnlpUrl' bestimmt.
      }
    }
    else
    {
      try
      {
        codebase = new URL(jnlpUrl, ".");
      }
      catch (MalformedURLException pE)
      {
        throw new RuntimeException(pE);
      }
    }

    jars = findChildElementsByPath("resources/jar");
    extensions = findChildElementsByPath("resources/extension");
  }

  long getLastModified()
  {
    try
    {
      return jnlpUrl.openConnection().getLastModified();
    }
    catch (IOException pE)
    {
      return 0;
    }
  }

  URL getCodebase()
  {
    return codebase;
  }

  Stream<JnlpReference> streamJarJnlpReferences()
  {
    return jars.stream().map(element -> new JnlpReference(getCodebase(), element));
  }

  private Stream<JnlpReference> _streamExtensionJnlpReferences()
  {
    return extensions.stream().map(element -> new JnlpReference(getCodebase(), element));
  }

  List<Element> findChildElementsByPath(String pPath)
  {
    List<Element> elements = Collections.singletonList(document.getDocumentElement());
    String[] pathElements = pPath.split("/");
    for (String pathElement : pathElements)
    {
      List<Element> list = new ArrayList<>();
      for (Element element : elements)
      {
        list.addAll(XMLUtil.findChildElements(element, pathElement));
      }
      elements = list;
    }
    return elements;
  }

}
