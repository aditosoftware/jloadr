package de.adito.jloadr.common;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
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

  public static String toSimpleInfo(Object pObject, String pDetail)
  {
    String identity = pObject.getClass().getSimpleName() + '@' + Integer.toHexString(System.identityHashCode(pObject));
    return identity + (pDetail == null || pDetail.isEmpty() ? "" : "(" + pDetail + ")");
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
    return Base64.getEncoder().encodeToString(digest).replaceAll("[^\\w_\\-\\.]", "");
  }

  public static void copy(InputStream pInputStream, OutputStream pOutputStream) throws IOException
  {
    try (OutputStream out = pOutputStream; InputStream in = pInputStream) {
      byte[] buffer = new byte[256 * 1024];
      int len;
      while ((len = in.read(buffer)) != -1)
        out.write(buffer, 0, len);
    }
  }

  public static void deleteEmptyDirectories(Path pPath) throws IOException
  {
    if (Files.isDirectory(pPath) && !Files.list(pPath).findAny().isPresent()) {
      Files.delete(pPath);
      deleteEmptyDirectories(pPath.getParent());
    }
  }

}
