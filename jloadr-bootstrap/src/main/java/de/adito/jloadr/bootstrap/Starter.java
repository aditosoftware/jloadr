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

  private static final String DEFAULT_MIN_JAVA_VERSION = "9";

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
      if(!(new File(System.getProperty("user.dir") + File.separator + "jre")
          .exists()))
        _checkJavaVersion(System.getProperty("java.version"));

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

  /**
   * This method checks if the used Java version is at least the minimum version. It only uses the first two instances
   * of the version number to compare the version.
   */

  private static void _checkJavaVersion(String pCurrent)
  {
    String minimalVersion = _testValidJavaFormat(System.getProperty("min.java"));
    String[] minimalArray = minimalVersion.split("\\.", 3);
    String[] currentArray = pCurrent.split("\\.", 3);

    int current = (Integer.parseInt(currentArray[0]) * 10) +
        (currentArray.length < 2 ? 0 : Integer.parseInt(currentArray[1]));

    int minimal = (Integer.parseInt(minimalArray[0]) * 10) +
        (minimalArray.length < 2 ? 0 : Integer.parseInt(minimalArray[1]));

    if(current < minimal)
      throw new RuntimeException("Your Java is outdated, please use at least Java " + minimalVersion);
  }

  /**
   * This method validates the given version number. Numbers that start with '-' or a number between '2' and '8' will
   * throw a RuntimeException. Versions like '1.8', '10', '10.2' etc are expected.
   * @return a String with a valid version number
   */
  private static String _testValidJavaFormat(String pVersion)
  {
    if (pVersion == null)
      return DEFAULT_MIN_JAVA_VERSION;

    String[] versionArray = pVersion.split("\\.", 3);
    int firstNumb;
    try
    {
      firstNumb = Integer.parseInt(versionArray[0]);
    }
    catch(NumberFormatException pE)
    {
      throw new RuntimeException("The VM parameter 'min.java' must be a valid version number.");
    }

    if(firstNumb > 1 && firstNumb < 9 || firstNumb <= 0)
      throw new RuntimeException("The VM parameter 'min.java' must be a valid version number.");

    return pVersion;
  }
}
