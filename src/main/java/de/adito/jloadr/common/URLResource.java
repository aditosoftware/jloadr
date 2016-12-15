package de.adito.jloadr.common;

import de.adito.jloadr.api.IResource;

import javax.annotation.Nonnull;
import java.io.*;
import java.net.*;
import java.util.Objects;

/**
 * @author j.boesl, 05.09.16
 */
public class URLResource implements IResource
{
  private URL url;
  private URLConnection urlConnection;

  public URLResource(URL pUrl)
  {
    url = pUrl;
  }

  public void checkAvailable() throws IOException
  {
    _getUrlConnection().getInputStream();
  }

  @Nonnull
  @Override
  public String getId()
  {
    return JLoadrUtil.getIdForUrl(url);
  }

  @Override
  public long getSize() throws IOException
  {
    return _getUrlConnection().getContentLengthLong();
  }

  @Override
  public long getLastModified() throws IOException
  {
    return _getUrlConnection().getLastModified();
  }

  @Nonnull
  @Override
  public String getHash()
  {
    return JLoadrUtil.hash(getId());
  }

  @Nonnull
  @Override
  public InputStream getInputStream() throws IOException
  {
    return _getUrlConnection().getInputStream();
  }

  private synchronized URLConnection _getUrlConnection() throws IOException
  {
    if (urlConnection == null) {
      urlConnection = url.openConnection();
      urlConnection.setUseCaches(true);
      urlConnection.connect();
    }
    return urlConnection;
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO)
      return true;
    if (pO == null || getClass() != pO.getClass())
      return false;
    URLResource that = (URLResource) pO;
    return Objects.equals(url, that.url);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(url);
  }

  @Override
  public String toString()
  {
    return JLoadrUtil.toSimpleInfo(this, url.toExternalForm());
  }

}
