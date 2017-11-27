package de.adito.jloadr.bootstrap;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * @author j.boesl, 27.11.17
 */
public class BootstrapUtil
{

  public static String readTextFromURL(URL pURL) throws IOException
  {
    try (InputStream inputStream = pURL.openStream();
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
    {
      copy(inputStream, outputStream);
      return outputStream.toString(StandardCharsets.UTF_8.name());
    }
  }

  public static void copy(InputStream pInputStream, OutputStream pOutputStream) throws IOException
  {
    try (OutputStream out = pOutputStream; InputStream in = pInputStream)
    {
      byte[] buffer = new byte[8192];
      int len;
      while ((len = in.read(buffer)) != -1)
        out.write(buffer, 0, len);
    }
  }

  public static URL getRelative(URL pParentUrl, String pUrlString) throws IllegalArgumentException
  {
    try
    {
      URI uri = new URI(pUrlString);
      if (uri.isAbsolute())
        return uri.toURL();
      if (pParentUrl == null)
        throw new IllegalArgumentException();

      URI parentUri = pParentUrl.toURI();
      return parentUri.resolve(pUrlString).toURL();
    }
    catch (URISyntaxException | MalformedURLException pE)
    {
      throw new IllegalArgumentException(pE);
    }
  }

  public static String getHash(InputStream pInputStream)
  {
    try (InputStream inputStream = pInputStream;
         DigestingInputStream digestingInputStream = new DigestingInputStream(inputStream))
    {
      int n = 0;
      while (n != -1)
        n = digestingInputStream.read();

      return digestingInputStream.getDigest();
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  public static String stackTraceToString(Throwable pE) throws IOException
  {
    try (StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw))
    {
      pE.printStackTrace(pw);
      return sw.toString();
    }
  }

}
