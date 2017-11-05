package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;
import de.adito.jloadr.repository.URLResource;

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
  private Map<IResourceId, IResource> resourceMap;


  protected JlrResourcePack(URL pJlrPackUrl, URL pResourcesUrl)
  {
    jlrPack = new JlrPack(pJlrPackUrl);
    resourcesUrl = pResourcesUrl;
  }

  @Override
  public String getId()
  {
    return JLoadrUtil.calculateHash(jlrPack.getUrl().toExternalForm());
  }

  @Override
  public List<? extends IResource> getResources()
  {
    return new ArrayList<>(_getResourceMap().values());
  }

  @Override
  public IResource getResource(IResourceId pId)
  {
    return _getResourceMap().get(pId);
  }

  private synchronized Map<IResourceId, IResource> _getResourceMap()
  {
    if (resourceMap == null)
    {
      resourceMap = new HashMap<>();
      jlrPack.loadPack();
      resourceMap = jlrPack.getEntries().stream()
          .map(entry -> {
            try
            {
              URL url = new URL(resourcesUrl, entry.getId().toString());
              return new JlrResource(entry, url);
            }
            catch (MalformedURLException pE)
            {
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

    @Override
    public IResourceId getId()
    {
      return entry.getId();
    }

    @Override
    public String getHash()
    {
      return entry.getHash();
    }
  }

}
