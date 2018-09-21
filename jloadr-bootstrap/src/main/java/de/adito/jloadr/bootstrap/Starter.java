package de.adito.jloadr.bootstrap;

import de.adito.trustmanager.TrustManagerSslContext;
import de.adito.trustmanager.store.JKSCustomTrustStore;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;

public class Starter
{

  private static final String JLOADR_JAR = "jloadr.jar";
  private static final String JLOADR_JAR_SHA1 = "jloadr.jar.sha1";

  public static void main(String[] args) throws Throwable
  {
    if (args.length == 0)
      throw new RuntimeException("first parameter must be the repository url");

    Path trustStorePath = Paths.get(JKSCustomTrustStore.TRUST_STORE_PATH).toAbsolutePath();
    System.setProperty(JKSCustomTrustStore.TURST_STORE_PATH_SYSTEM_PROPERTY, trustStorePath.toString());
    System.setProperty("jloadr." + JKSCustomTrustStore.TURST_STORE_PATH_SYSTEM_PROPERTY, trustStorePath.toString());
    TrustManagerSslContext.initSslContext();

    String useSystemProxies = System.getProperty("java.net.useSystemProxies");
    if (useSystemProxies == null)
      System.setProperty("java.net.useSystemProxies", "true");

    Throwable loadError = null;
    try
    {
      URL url = BootstrapUtil.getMoved(new URL(args[0]));
      _loadNewVersion(url);
      args[0] = url.toExternalForm();
    }
    catch (Throwable pE)
    {
      loadError = pE;
    }
    try
    {
      _runMain(Paths.get(JLOADR_JAR), "de.adito.jloadr.Main", args);
    }
    catch (Throwable pE)
    {
      String exceptionMessage = "";
      if (loadError != null)
        exceptionMessage += BootstrapUtil.stackTraceToString(loadError) + "\n\n";
      exceptionMessage += BootstrapUtil.stackTraceToString(pE);
      _showError(exceptionMessage);
      throw loadError == null ? pE : loadError;
    }
  }

  private static void _loadNewVersion(URL pUrl) throws IOException
  {
    URL jloadrJarUrl = BootstrapUtil.getRelative(pUrl, JLOADR_JAR);
    URL jloadrJarChecksumUrl = BootstrapUtil.getRelative(pUrl, JLOADR_JAR_SHA1);

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

      URLConnection urlConnection = jloadrJarUrl.openConnection();
      if (urlConnection instanceof HttpURLConnection && ((HttpURLConnection) urlConnection).getResponseCode() != 200)
        throw new RuntimeException("Error loading '" + jloadrJarUrl.toExternalForm() + "': " +
                                       ((HttpURLConnection) urlConnection).getResponseMessage());

      try (InputStream inputStream = urlConnection.getInputStream();
           OutputStream outputStream = Files.newOutputStream(localJarPath))
      {
        BootstrapUtil.copy(inputStream, outputStream);
      }
    }
  }

  private static void _runMain(Path pLocalJar, String pClassName, String... pArgs)
      throws Throwable
  {
    URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{pLocalJar.toUri().toURL()});
    // required for ServiceLoader
    Thread.currentThread().setContextClassLoader(urlClassLoader);

    Class<?> targetClass = Class.forName(pClassName, true, urlClassLoader);
    Method main = targetClass.getMethod("main", String[].class);
    try
    {
      main.invoke(null, (Object) pArgs);
    }
    catch (InvocationTargetException pE)
    {
      throw pE.getCause();
    }
  }

  private static void _showError(String pMessage)
  {
    String title = UIManager.getString("OptionPane.messageDialogTitle", null);
    JTextArea textArea = new JTextArea(pMessage);
    JScrollPane scrollPane = new JScrollPane(textArea);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    scrollPane.setPreferredSize(new Dimension(800, 400));
    JOptionPane.showMessageDialog(null, scrollPane, title, JOptionPane.ERROR_MESSAGE);
  }

}
