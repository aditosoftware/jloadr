package de.adito.jloadr.repository.jnlp;

import org.w3c.dom.Element;

import java.net.*;
import java.util.Objects;

/**
 * @author j.boesl, 06.09.16
 */
class JnlpReference
{

  private URL codebase;
  private Element jarElement;

  JnlpReference(URL pCodebase, Element pJarElement)
  {
    codebase = pCodebase;
    jarElement = pJarElement;
  }

  URL getUrl()
  {
    try {
      return new URL(codebase, jarElement.getAttribute("href"));
    }
    catch (MalformedURLException pE) {
      throw new RuntimeException(pE);
    }
  }

  URL getCodebase()
  {
    return codebase;
  }

  Element getJarElement()
  {
    return jarElement;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO)
      return true;
    if (pO == null || getClass() != pO.getClass())
      return false;
    JnlpReference that = (JnlpReference) pO;
    return Objects.equals(codebase, that.codebase) &&
        Objects.equals(jarElement, that.jarElement);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(codebase, jarElement);
  }
}
