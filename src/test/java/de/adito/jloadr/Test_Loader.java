package de.adito.jloadr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.repository.jlr.JlrResourcePack;
import de.adito.jloadr.repository.jnlp.JnlpResourcePack;
import de.adito.jloadr.repository.local.LocalStore;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author j.boesl, 09.01.17
 */
public class Test_Loader
{
  private File dir;

  @Before
  public void prepare()
  {
    dir = new File("target", UUID.randomUUID().toString());
    dir.mkdir();
  }

  @After
  public void cleanup() throws IOException
  {
    Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>()
    {
      @Override
      public FileVisitResult visitFile(Path pFile, BasicFileAttributes attrs) throws IOException
      {
        Files.delete(pFile);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path pDir, IOException exc) throws IOException
      {
        Files.delete(pDir);
        return FileVisitResult.CONTINUE;
      }
    });
  }

  @Test
  public void testLoader() throws IOException, InterruptedException
  {
    List<String> fileNames = _createFileNames();
    _createTestFiles(dir, fileNames);
    File mainJnlp = _createJnlps(dir, fileNames);
    Path store1Path = dir.toPath().resolve("store1");
    Path store2Path = dir.toPath().resolve("store2");

    IResourcePack remoteResourcePack = new JnlpResourcePack(mainJnlp.toURI().toURL());
    IStore localStore = new LocalStore(store1Path);
    Thread.sleep(1000);
    IStoreResourcePack localResourcePack = new Loader().load(localStore, remoteResourcePack, null);
    _check(remoteResourcePack, localResourcePack);

    Path localResourcePackPath = store1Path.resolve(localResourcePack.getId() + ".jlr.xml");
    remoteResourcePack = new JlrResourcePack(localResourcePackPath.toUri().toURL());
    localStore = new LocalStore(store2Path);
    Thread.sleep(1000);
    localResourcePack = new Loader().load(localStore, remoteResourcePack, null);
    _check(remoteResourcePack, localResourcePack);


    JLoaderConfig loaderConfig = new JLoaderConfig();
    IStoreResource configResource = localResourcePack.getResource(JLoaderConfig.CONFIG_NAME);
    Assert.assertNotNull(configResource);
    try (InputStream inputStream = configResource.getInputStream())
    {
      loaderConfig.load(inputStream);
    }

    Process process = new ProcessBuilder(loaderConfig.getStartCommands())
        .directory(store2Path.resolve(localResourcePack.getId()).toAbsolutePath().toFile())
        .start();

    process.waitFor(10, TimeUnit.SECONDS);
    Assert.assertEquals(0, process.exitValue());

    String sout = _readString(process.getInputStream());
    String serr = _readString(process.getErrorStream());

    Assert.assertEquals("arg1\ntrue\n", sout);
    Assert.assertEquals("arg2\n", serr);
  }

  private void _check(IResourcePack pRemoteResourcePack, IResourcePack pLocalResourcePack) throws IOException
  {
    for (IResource rResource : pRemoteResourcePack.getResources())
    {
      String id = rResource.getId();
      IResource lResource = pLocalResourcePack.getResource(id);
      Assert.assertNotNull("no local resource for '" + rResource.getId() + "'.", lResource);
      Assert.assertEquals("id mismatch for '" + id + "'.", id, lResource.getId());
      Assert.assertEquals("size mismatch for '" + id + "'.", rResource.getSize(), lResource.getSize());
      Assert.assertEquals("lastModified mismatch for '" + id + "'.", rResource.getLastModified(), lResource.getLastModified());
      if (rResource.getHash() != null && lResource.getHash() != null)
        Assert.assertEquals("hash mismatch for '" + id + "'.", rResource.getHash(), lResource.getHash());
    }
  }

  private void _createTestFiles(File pDir, List<String> pNames) throws IOException
  {
    for (String fileName : pNames)
    {
      File file = new File(pDir, fileName);
      file.createNewFile();
      try (FileOutputStream outputStream = new FileOutputStream(file))
      {
        for (int j = 0; j < 1024; j++)
          outputStream.write((int) (200 * Math.random()) + 1);
      }
    }
    for (Path path : Files.newDirectoryStream(Paths.get("target"), "jloadr-*-tests.jar"))
      Files.copy(path, dir.toPath().resolve("jloadr-tests.jar"));
  }

  private List<String> _createFileNames()
  {
    List<String> fileNames = new ArrayList<>();
    for (int i = 0; i < 20; i++)
    {
      int number = (int) (Math.random() * i) + 1;
      String fileName = "testFile_" + number + ".txt";
      if (!fileNames.contains(fileName))
        fileNames.add(fileName);
    }
    return fileNames;
  }

  private File _createJnlps(File pDir, List<String> pResources) throws IOException
  {
    int splitIndex = pResources.size() / 2;
    String res1 = "";
    String res2 = "";
    String refFormat = "<jar href=\"{0}\"/>";
    for (int i = 0; i < pResources.size(); i++)
    {
      String format = MessageFormat.format(refFormat, pResources.get(i));
      if (i < splitIndex)
        res1 += (res1.isEmpty() ? "" : "\n") + format;
      else
        res2 += (res2.isEmpty() ? "" : "\n") + format;
    }
    _createJnlp(pDir, "libs.jnlp", res2);
    return _createJnlp(pDir, "test.jnlp", pDir.toURI().toURL().toExternalForm(), res1);
  }

  private File _createJnlp(File pDir, String pName, String... pArgs) throws IOException
  {
    String content;
    try (InputStream inputStream = Test_Loader.class.getResourceAsStream(pName + ".template"))
    {
      content = MessageFormat.format(_readString(inputStream), (Object[]) pArgs);
    }
    File file = new File(pDir, pName);
    try (PrintStream printStream = new PrintStream(new FileOutputStream(file)))
    {
      printStream.write(content.getBytes("utf-8"));
    }
    return file;
  }

  private static String _readString(InputStream pInputStream)
  {
    java.util.Scanner s = new java.util.Scanner(pInputStream).useDelimiter("\\A");
    return s.hasNext() ? s.next() : "";
  }


}