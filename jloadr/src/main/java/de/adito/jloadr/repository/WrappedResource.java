package de.adito.jloadr.repository;

import de.adito.jloadr.api.*;

import java.io.*;

/**
 * @author j.boesl, 12.10.17
 */
public class WrappedResource implements IResource
{
  private IResource resource;

  public WrappedResource(IResource pResource)
  {
    resource = pResource;
  }

  @Override
  public IResourceId getId()
  {
    return resource.getId();
  }

  @Override
  public InputStream getInputStream() throws IOException
  {
    return resource.getInputStream();
  }

  @Override
  public long getSize() throws IOException
  {
    return resource.getSize();
  }

  @Override
  public long getLastModified() throws IOException
  {
    return resource.getLastModified();
  }

  @Override
  public String getHash()
  {
    return resource.getHash();
  }
}
