package de.adito.jloadr.repository;

import de.adito.jloadr.api.IResourceId;

import java.io.File;
import java.nio.file.*;
import java.util.Objects;

/**
 * @author j.boesl, 24.01.17
 */
public class ResourceId implements IResourceId
{
  private String id;

  public ResourceId(String pId)
  {
    id = pId;
  }

  public ResourceId(Path pPath)
  {
    id = pPath.toString().replace(File.separatorChar, '/');
  }

  @Override
  public Path toPath()
  {
    return Paths.get(id.replace('/', File.separatorChar));
  }

  @Override
  public String toString()
  {
    return id;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO)
      return true;
    if (!(pO instanceof ResourceId))
      return false;
    ResourceId that = (ResourceId) pO;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(id);
  }
}
