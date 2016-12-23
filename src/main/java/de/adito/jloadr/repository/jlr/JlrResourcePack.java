package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;

import javax.annotation.*;
import java.io.File;
import java.net.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrResourcePack implements IResourcePack
{
  private JlrPack jlrPack;
  private URL resourcesUrl;
  private Map<String, IResource> resourceMap;


  public JlrResourcePack(URL pJlrPackUrl)
  {
    this(new JlrPack(pJlrPackUrl));
  }

  public JlrResourcePack(JlrPack pJlrPack)
  {
    jlrPack = pJlrPack;
    try {
      String urlString = jlrPack.getUrl().toExternalForm();
      int index = urlString.lastIndexOf(".jlr.xml");
      if (index != -1)
        urlString = urlString.substring(0, index);
      resourcesUrl = new URL(urlString + "/");
    }
    catch (MalformedURLException pE) {
      throw new RuntimeException(pE);
    }
  }

  @Nonnull
  @Override
  public String getId()
  {
    return JLoadrUtil.getHash(jlrPack.getUrl().toExternalForm()).replaceAll(File.separator, "");
  }

  @Nonnull
  @Override
  public List<? extends IResource> getResources()
  {
    return new ArrayList<>(_getResourceMap().values());
  }

  @Nullable
  @Override
  public IResource getResource(@Nonnull String pId)
  {
    return _getResourceMap().get(pId);
  }

  private synchronized Map<String, IResource> _getResourceMap()
  {
    if (resourceMap == null) {
      resourceMap = new HashMap<>();
      jlrPack.loadPack();
      resourceMap = jlrPack.getEntries().stream()
          .map(entry -> {
            try {
              URL url = new URL(resourcesUrl, entry.getId());
              return new JlrResource(entry, url);
            }
            catch (MalformedURLException pE) {
              throw new RuntimeException(pE);
            }
          })
          .collect(Collectors.toMap(URLResource::getId, Function.identity()));
    }
    return resourceMap;
  }


  private static class JlrResource extends URLResource
  {
    private JlrEntry entry;

    private JlrResource(JlrEntry pEntry, URL pUrl)
    {
      super(pUrl);
      entry = pEntry;
    }

    @Nonnull
    @Override
    public String getId()
    {
      return entry.getId();
    }

    @Nonnull
    @Override
    public String getHash()
    {
      return entry.getHash();
    }
  }

}
