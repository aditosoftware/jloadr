package de.adito.jloadr.common;

import javax.annotation.*;
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

  public static String hash(String pId)
  {
    return Base64.getEncoder().encodeToString(_hashToBytes(pId));
  }

  private static byte[] _hashToBytes(String pId)
  {
    try {
      byte[] idBytes = pId.getBytes(Charset.forName("utf-8"));
      return MessageDigest.getInstance("sha-1").digest(idBytes);
    }
    catch (NoSuchAlgorithmException pE) {
      throw new RuntimeException(pE);
    }
  }

}
