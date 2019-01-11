package de.adito.jloadr.bootstrap;

import java.io.*;
import java.security.*;
import java.util.Base64;

public class DigestingInputStream extends BufferedInputStream
{
  private MessageDigest md = getMessageDigest();
  private String digest;

  public DigestingInputStream(InputStream in)
  {
    super(in);
  }

  @Override
  public synchronized int read() throws IOException
  {
    int read = super.read();
    if (read != -1)
      md.update((byte) read);
    return read;
  }

  @Override
  public synchronized int read(byte[] b, int off, int len) throws IOException
  {
    int read = super.read(b, off, len);
    if (read != -1)
      md.update(b, off, read);
    return read;
  }

  public synchronized String getDigest()
  {
    if (md != null)
    {
      digest = Base64.getEncoder().encodeToString(md.digest());
      md = null;
    }
    return digest;
  }

  public static MessageDigest getMessageDigest()
  {
    try
    {
      return MessageDigest.getInstance("SHA-1");
    }
    catch (NoSuchAlgorithmException pE)
    {
      throw new RuntimeException("Was not able to digest message.");
    }
  }
}
