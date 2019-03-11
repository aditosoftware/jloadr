package de.adito.jloadr.bootstrap;

import de.adito.trustmanager.TrustManagerSslContext;
import de.adito.trustmanager.store.JKSCustomTrustStore;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;

public class Starter
{

  private static final String JLOADR_JAR = "jloadr.jar";
  private static final String JLOADR_JAR_SHA1 = "jloadr.jar.sha1";

  private static final String MIN_JAVA_VERSION = "1.9";
  public static boolean CHECK_JAVA_VERSION = true;

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
      if(CHECK_JAVA_VERSION)
        _checkJavaVersion(MIN_JAVA_VERSION, System.getProperty("java.version"));

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
      new ShowErrorUtil(loadError, pE).showError();
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

  private static void _runMain(Path pLocalJar, String pClassName, String... pArgs) throws Throwable
  {
    //MalformedURLException
    URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{pLocalJar.toUri().toURL()});
    // required for ServiceLoader
    Thread.currentThread().setContextClassLoader(urlClassLoader);

    //ClassNotFoundException
    Class<?> targetClass = Class.forName(pClassName, true, urlClassLoader);
    //NoSuchMethodException
    Method main = targetClass.getMethod("main", String[].class);
    try
    {
      //IllegalAccessException, InvocationTargetException
      main.invoke(null, (Object) pArgs);
    }
    catch (InvocationTargetException pE)
    {
      throw pE.getCause();
    }
  }

  /**
   * This method checks if the used Java version is at least the minimum version. It only uses the first two instances
   * of the version number to compare the versions
   */

  private static void _checkJavaVersion(String pMinimal, String pCurrent)
  {
    String[] minimalArray = pMinimal.split("\\.", 3);
    String[] currentArray = pCurrent.split("\\.", 3);

    int current = (Integer.parseInt(currentArray[0]) * 10) +
        (currentArray.length < 2 ? 0 : Integer.parseInt(currentArray[1]));

    int minimal = (Integer.parseInt(minimalArray[0]) * 10) +
        (minimalArray.length < 2 ? 0 : Integer.parseInt(minimalArray[1]));

    if(current < minimal)
      throw new RuntimeException("Your Java is outdated, please use at least Java " + MIN_JAVA_VERSION);
  }
}
