package de.adito.jloadr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.repository.jlr.JlrResourcePack;
import de.adito.jloadr.repository.jnlp.JnlpResourcePack;
import de.adito.jloadr.repository.local.LocalStore;
import org.junit.*;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.*;

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
  public void testLoad() throws IOException, InterruptedException
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
  }

  private void _check(IResourcePack pRemoteResourcePack, IResourcePack pLocalResourcePack) throws IOException
  {
    for (IResource rResource : pRemoteResourcePack.getResources()) {
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
    for (String fileName : pNames) {
      File file = new File(pDir, fileName);
      file.createNewFile();
      try (FileOutputStream outputStream = new FileOutputStream(file)) {
        for (int j = 0; j < 1024; j++)
          outputStream.write((int) (200 * Math.random()) + 1);
      }
    }
  }

  private List<String> _createFileNames()
  {
    List<String> fileNames = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
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
    for (int i = 0; i < pResources.size(); i++) {
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
    try (InputStream inputStream = Test_Loader.class.getResourceAsStream(pName + ".template")) {
      java.util.Scanner s = new java.util.Scanner(inputStream).useDelimiter("\\A");
      content = MessageFormat.format(s.hasNext() ? s.next() : "", (Object[]) pArgs);
    }
    File file = new File(pDir, pName);
    try (PrintStream printStream = new PrintStream(new FileOutputStream(file))) {
      printStream.write(content.getBytes("utf-8"));
    }
    return file;
  }


}