package de.adito.jloadr.repository.jlr.config;

import de.adito.jloadr.common.XMLUtil;
import org.w3c.dom.*;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrEntry
{
  private String id;
  private String hash;
  private String sig;


  public JlrEntry(Element pElement)
  {
    this(XMLUtil.getChildText(pElement, "id"),
         XMLUtil.getChildText(pElement, "hash"),
         XMLUtil.getChildText(pElement, "sig"));
  }

  public JlrEntry(String pId, String pHash, String pSig)
  {
    id = pId;
    hash = pHash;
    sig = pSig;
  }

  public JlrEntry()
  {
    this(null, null, null);
  }

  public String getId()
  {
    return id;
  }

  public void setId(String pId)
  {
    id = pId;
  }

  public String getHash()
  {
    return hash;
  }

  public void setHash(String pHash)
  {
    hash = pHash;
  }

  public String getSig()
  {
    return sig;
  }

  public void setSig(String pSig)
  {
    sig = pSig;
  }


  public void appendToNode(Node pNode)
  {
    Document doc = pNode.getOwnerDocument();

    Element entry = doc.createElement("entry");
    pNode.appendChild(entry);

    Element id = doc.createElement("id");
    id.setTextContent(getId());
    entry.appendChild(id);

    if (getHash() != null) {
      Element hash = doc.createElement("hash");
      hash.setTextContent(getHash());
      entry.appendChild(hash);
    }

    if (getSig() != null) {
      Element sig = doc.createElement("sig");
      sig.setTextContent(getSig());
      entry.appendChild(sig);
    }
  }
}
