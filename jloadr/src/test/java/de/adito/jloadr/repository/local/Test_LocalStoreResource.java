package de.adito.jloadr.repository.local;

import de.adito.jloadr.common.JLoadrUtil;
import de.adito.jloadr.repository.ResourceId;
import de.adito.jloadr.repository.jlr.JlrEntry;
import org.junit.*;

import java.io.*;
import java.nio.file.*;

/**
 * @author j.boesl, 13.10.17
 */
public class Test_LocalStoreResource
{

  @Test
  public void testHash() throws IOException
  {
    Path path = Paths.get("test");
    StringBuffer data = new StringBuffer();
    for (int i = 0; i < 10 * 1024 * 1024; i++)
      data.append(64 + (13 * i % 64));


    try
    {
      JlrEntry jlrEntry = new JlrEntry(new ResourceId(""));
      LocalStoreResource resource = new LocalStoreResource(jlrEntry, path);
      try (OutputStream outputStream = resource.getOutputStream();
           InputStream inputStream = new ByteArrayInputStream(data.toString().getBytes()))
      {
        JLoadrUtil.copy(inputStream, outputStream);
      }

      try (InputStream inputStream = new ByteArrayInputStream(data.toString().getBytes()))
      {
        String hash = JLoadrUtil.getHash(inputStream);
        Assert.assertEquals(hash, resource.getHash());
        jlrEntry.setHash(null);
        Assert.assertEquals(hash, resource.getHash());
      }


    }
    finally
    {
      Files.deleteIfExists(path);
    }
  }

}