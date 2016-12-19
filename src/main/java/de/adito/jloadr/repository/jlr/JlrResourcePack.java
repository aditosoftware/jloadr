package de.adito.jloadr.repository.jlr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;
import de.adito.jloadr.repository.jlr.config.JlrPack;

import javax.annotation.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 19.12.16
 */
public class JlrResourcePack implements IResourcePack
{
  private JlrPack jlrPack;
  private Map<String, IResource> resourceMap;


  public JlrResourcePack(JlrPack pJlrPack)
  {
    jlrPack = pJlrPack;
  }

  @Nonnull
  @Override
  public String getId()
  {
    return JLoadrUtil.getIdForUrl(jlrPack.getUrl());
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
      return jlrPack.getEntries().stream()
          .map(entry -> new URLResource(entry.getUrl()))
          .collect(Collectors.toMap(URLResource::getId, Function.identity()));
    }
    return resourceMap;
  }
}
