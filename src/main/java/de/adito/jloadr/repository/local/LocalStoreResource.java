package de.adito.jloadr.repository.local;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;
import de.adito.jloadr.repository.jlr.JlrEntry;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.*;

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
    return new BufferedOutputStream(Files.newOutputStream(path))
    {
      private MessageDigest md = JLoadrUtil.getMessageDigest();

      @Override
      public synchronized void flush() throws IOException
      {
        if (count > 0)
        {
          md.update(buf, 0, count);
        }
        super.flush();
      }

      @Override
      public void close() throws IOException
      {
        super.close();
        if (md != null)
        {
          jlrEntry.setHash(Base64.getEncoder().encodeToString(md.digest()));
          md = null;
        }
      }
    };
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
    String hash = jlrEntry.getHash();
    if (hash == null)
    {
      try (InputStream inputStream = getInputStream())
      {
        hash = JLoadrUtil.getHash(inputStream);
        jlrEntry.setHash(hash);
      }
      catch (IOException pE)
      {
        throw new RuntimeException(pE);
      }
    }
    return hash;
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
