package de.adito.jloadr.common;

import de.adito.jloadr.api.IResource;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/**
 * @author j.boesl, 05.09.16
 */
public class JLoadrUtil
{

  private static final String PREFIX_ADITO = "adito.";
  private static final String PREFIX_JLOADR = "jloadr.";

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
    try
    {
      return MessageDigest.getInstance("SHA-1");
    }
    catch (NoSuchAlgorithmException pE)
    {
      throw new RuntimeException();
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

  public static String calculateHash(IResource pResource)
  {
    try (InputStream inputStream = pResource.getInputStream())
    {
      return getHash(inputStream);
    }
    catch (IOException pE)
    {
      throw new RuntimeException(pE);
    }
  }

  public static String calculateHash(String pId)
  {
    byte[] idBytes = pId.getBytes(Charset.forName("utf-8"));
    byte[] digest = getMessageDigest().digest(idBytes);
    return Base64.getEncoder().encodeToString(digest).replaceAll("[^\\w_\\-\\.]", "");
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

  public static void deleteEmptyDirectories(Path pPath) throws IOException
  {
    if (Files.isDirectory(pPath) && !Files.list(pPath).findAny().isPresent())
    {
      Files.delete(pPath);
      deleteEmptyDirectories(pPath.getParent());
    }
  }

  public static List<String> getAdditionalSystemParameters()
  {
    List<String> additionalSystemParameters = new ArrayList<>();
    for (Map.Entry<Object, Object> entry : System.getProperties().entrySet())
    {
      String key = entry.getKey().toString();
      String parameter = null;
      if (key.startsWith(PREFIX_ADITO))
        parameter = key;
      else if (key.startsWith(PREFIX_JLOADR))
        parameter = key.substring(PREFIX_JLOADR.length());

      if (parameter != null)
      {
        if (entry.getValue() != null && !entry.getValue().toString().isEmpty())
          parameter += "=" + entry.getValue().toString();
        additionalSystemParameters.add(parameter);
      }
    }
    return additionalSystemParameters;
  }

}
