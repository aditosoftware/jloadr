package de.adito.jloadr.repository.local;

import de.adito.jloadr.api.IStoreResource;
import de.adito.jloadr.common.JLoadrUtil;

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.file.*;
import java.util.Objects;

/**
 * @author j.boesl, 08.09.16
 */
public class LocalStoreResource implements IStoreResource
{
  private String id;
  private Path path;

  public LocalStoreResource(String pId, Path pPath)
  {
    id = pId;
    path = pPath;
  }

  @Override
  public OutputStream getOutputStream() throws IOException
  {
    return Files.newOutputStream(path);
  }

  @Nonnull
  @Override
  public String getId()
  {
    return id;
  }

  @Nonnull
  @Override
  public InputStream getInputStream() throws IOException
  {
    return Files.newInputStream(path);
  }

  @Override
  public long getSize() throws IOException
  {
    return Files.size(path);
  }

  @Override
  public long getLastModified() throws IOException
  {
    return Files.getLastModifiedTime(path).toMillis();
  }

  @Nonnull
  @Override
  public String getHash()
  {
    return JLoadrUtil.hash(getId());
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO)
      return true;
    if (pO == null || getClass() != pO.getClass())
      return false;
    LocalStoreResource that = (LocalStoreResource) pO;
    return Objects.equals(path, that.path);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(path);
  }

  @Override
  public String toString()
  {
    return JLoadrUtil.toSimpleInfo(this, id);
  }

}
