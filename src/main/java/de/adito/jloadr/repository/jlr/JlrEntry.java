package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.api.IResourceId;
import de.adito.jloadr.common.XMLUtil;
import de.adito.jloadr.repository.ResourceId;
import org.w3c.dom.*;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrEntry
{
  private IResourceId id;
  private String hash;
  private String sig;


  public JlrEntry(Element pElement)
  {
    id = new ResourceId(XMLUtil.getChildText(pElement, "id"));
    hash = XMLUtil.getChildText(pElement, "hash");
    sig = XMLUtil.getChildText(pElement, "sig");
  }

  public JlrEntry(IResourceId pId)
  {
    id = pId;
  }

  public IResourceId getId()
  {
    return id;
  }

  public void setId(IResourceId pId)
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
    id.setTextContent(getId().toString());
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
