package de.adito.jloadr.repository.local;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;
import de.adito.jloadr.repository.jlr.JlrEntry;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

/**
 * @author j.boesl, 08.09.16
 */
public class LocalStoreResource implements IStoreResource
{
  private JlrEntry jlrEntry;
  private Path path;

  public LocalStoreResource(JlrEntry pJlrEntry, Path pPath)
  {
    jlrEntry = pJlrEntry;
    path = pPath;
  }

  @Override
  public OutputStream getOutputStream() throws IOException
  {
    return Files.newOutputStream(path);
  }

  @Override
  public IResourceId getId()
  {
    return jlrEntry.getId();
  }

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

  @Override
  public void setLastModified(long pTime)
  {
    try
    {
      Files.setLastModifiedTime(path, FileTime.fromMillis(pTime));
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public String getHash()
  {
    return jlrEntry.getHash();
  }

  @Override
  public void setHash(String pHash)
  {
    jlrEntry.setHash(pHash);
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
    return JLoadrUtil.toSimpleInfo(this, getId().toPath().toString());
  }

}
