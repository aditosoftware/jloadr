package de.adito.jloadr.bootstrap;

import javax.swing.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;

public class Starter
{

  private static final String JLOADR_JAR = "jloadr.jar";
  private static final String JLOADR_JAR_SHA1 = "jloadr.jar.sha1";

  public static void main(String[] args) throws IOException
  {
    if (args.length == 0)
      throw new RuntimeException("first parameter must be the repository url");

    String url = args[0];

    try
    {
      _loadNewVersion(url);
    }
    catch (IOException pE)
    {
      // ignore for now
    }

    try
    {
      _runMain(Paths.get(JLOADR_JAR), "de.adito.jloadr.Main", url);
    }
    catch (Throwable pE)
    {
      JOptionPane.showMessageDialog(null, BootstrapUtil.stackTraceToString(pE), UIManager.getString(
          "OptionPane.messageDialogTitle", null), JOptionPane.ERROR_MESSAGE);
    }
  }

  private static void _loadNewVersion(String pUrl) throws IOException
  {
    URL jloadrJarUrl = BootstrapUtil.getRelative(new URL(pUrl), JLOADR_JAR);
    URL jloadrJarChecksumUrl = BootstrapUtil.getRelative(new URL(pUrl), JLOADR_JAR_SHA1);

    String remoteHash = BootstrapUtil.readTextFromURL(jloadrJarChecksumUrl);
    boolean differs = true;

    Path localJarPath = Paths.get(JLOADR_JAR);
    if (Files.isRegularFile(localJarPath))
    {
      try (InputStream inputStream = Files.newInputStream(localJarPath))
      {
        differs = !remoteHash.equalsIgnoreCase(BootstrapUtil.getHash(inputStream));
      }
    }
    if (differs)
    {
      try (InputStream inputStream = jloadrJarUrl.openStream();
           OutputStream outputStream = Files.newOutputStream(localJarPath))
      {
        BootstrapUtil.copy(inputStream, outputStream);
      }
    }
  }

  private static void _runMain(Path pLocalJar, String pClassName, String... pArgs)
      throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, MalformedURLException
  {
    URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{pLocalJar.toUri().toURL()});
    // required for ServiceLoader
    Thread.currentThread().setContextClassLoader(urlClassLoader);

    Class<?> targetClass = Class.forName(pClassName, true, urlClassLoader);
    Method main = targetClass.getMethod("main", String[].class);
    main.invoke(null, (Object) pArgs);
  }

}
