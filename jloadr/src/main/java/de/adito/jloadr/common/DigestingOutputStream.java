package de.adito.jloadr.common;

import java.io.*;
import java.security.MessageDigest;
import java.util.Base64;

public class DigestingOutputStream extends BufferedOutputStream
{
  private MessageDigest md = JLoadrUtil.getMessageDigest();
  private String digest;

  public DigestingOutputStream(OutputStream out)
  {
    super(out);
  }

  public DigestingOutputStream(OutputStream out, int size)
  {
    super(out, size);
  }

  @Override
  public synchronized void write(int b) throws IOException
  {
    super.write(b);
    md.update((byte) b);
  }

  @Override
  public synchronized void write(byte[] b, int off, int len) throws IOException
  {
    super.write(b, off, len);
    md.update(b, off, len);
  }

  @Override
  public synchronized void close() throws IOException
  {
    super.close();
    if (md != null)
    {
      digest = Base64.getEncoder().encodeToString(md.digest());
      md = null;
    }
  }

  public synchronized String getDigest()
  {
    if (digest == null)
      throw new IllegalStateException();
    return digest;
  }
}
