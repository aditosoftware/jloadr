package de.adito.jloadr.repository.mux;

import de.adito.jloadr.ResourcePackFactory;
import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;
import org.w3c.dom.Document;

import javax.annotation.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 25.01.17
 */
public class MuxResourcePack implements IResourcePack
{
  private URL packUrl;
  private List<IResourcePack> packs;

  protected MuxResourcePack(URL pUrl)
  {
    packUrl = pUrl;
    Document document = XMLUtil.loadDocument(packUrl);
    packs = XMLUtil.findChildElements(document.getDocumentElement(), "pack").stream()
        .filter(element -> {
          String os = element.getAttribute("os");
          if (os == null)
            return true;

          if (OsUtil.getOsType().toString().equalsIgnoreCase("os")) {
            String bitness = element.getAttribute("bitness");
            return bitness == null || OsUtil.getBitness().toString().equalsIgnoreCase(bitness);
          }
          return false;
        })
        .map(element -> {
          try {
            return ResourcePackFactory.get(UrlUtil.getRelative(packUrl, element.getTextContent().trim()));
          }
          catch (RuntimeException pE) {
            return null;
          }
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Nonnull
  @Override
  public String getId()
  {
    return JLoadrUtil.getHash(packUrl.toExternalForm());
  }

  @Nonnull
  @Override
  public List<? extends IResource> getResources()
  {
    Set<String> ids = new HashSet<>();
    return packs.stream()
        .flatMap(packs -> packs.getResources().stream())
        .filter(pResource -> ids.add(pResource.getId().toString()))
        .collect(Collectors.toList());
  }

  @Nullable
  @Override
  public IResource getResource(@Nonnull IResourceId pId)
  {
    return packs.stream()
        .map(pack -> getResource(pId))
        .filter(Objects::nonNull)
        .findFirst().orElse(null);
  }
}
