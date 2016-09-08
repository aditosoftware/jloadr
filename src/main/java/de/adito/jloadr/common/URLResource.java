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

  public URLResource(URL pUrl)
  {
    url = pUrl;
  }

  @Nonnull
  @Override
  public String getId()
  {
    return JLoadrUtil.getIdForUrl(url);
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
    URLConnection urlConnection = url.openConnection();
    urlConnection.connect();
    return urlConnection.getInputStream();
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
