package de.adito.jloadr.common;

import java.io.*;
import java.security.MessageDigest;
import java.util.Base64;

public class DigestingInputStream extends BufferedInputStream
{
  private MessageDigest md = JLoadrUtil.getMessageDigest();
  private String digest;

  public DigestingInputStream(InputStream in)
  {
    super(in);
  }

  public DigestingInputStream(InputStream in, int size)
  {
    super(in, size);
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
}
