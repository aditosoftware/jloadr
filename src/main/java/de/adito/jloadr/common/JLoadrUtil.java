package de.adito.jloadr.common;

import javax.annotation.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.*;
import java.util.Base64;

/**
 * @author j.boesl, 05.09.16
 */
public class JLoadrUtil
{

  private JLoadrUtil()
  {
  }

  public static String toSimpleInfo(@Nonnull Object pObject, @Nullable String pDetail)
  {
    String identity = pObject.getClass().getSimpleName() + '@' + Integer.toHexString(System.identityHashCode(pObject));
    return identity + (pDetail == null || pDetail.isEmpty() ? "" : "(" + pDetail + ")");
  }

  public static String normalizeId(String pId)
  {
    return pId.replaceAll("/", "&frasl;");
  }

  public static String getIdForUrl(URL pURL)
  {
    String host = pURL.getHost();
    int port = pURL.getPort();
    String path = pURL.getPath();
    return host + (port == -1 ? "" : "." + port) + (path.startsWith("/") ? "" : "/") + path;
  }

  public static MessageDigest getMessageDigest()
  {
    try {
      return MessageDigest.getInstance("SHA-1");
    }
    catch (NoSuchAlgorithmException pE) {
      throw new RuntimeException();
    }
  }

  public static String getHash(InputStream pInputStream)
  {
    try {
      MessageDigest digest = getMessageDigest();
      int n = 0;
      byte[] buffer = new byte[8192];
      while (n != -1) {
        n = pInputStream.read(buffer);
        if (n > 0) {
          digest.update(buffer, 0, n);
        }
      }
      return Base64.getEncoder().encodeToString(digest.digest());
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  public static String getHash(String pId)
  {
    byte[] idBytes = pId.getBytes(Charset.forName("utf-8"));
    byte[] digest = getMessageDigest().digest(idBytes);
    return Base64.getEncoder().encodeToString(digest);
  }

}
