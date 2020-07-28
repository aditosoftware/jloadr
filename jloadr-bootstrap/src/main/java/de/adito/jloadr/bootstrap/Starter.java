package de.adito.jloadr.bootstrap;

import de.adito.trustmanager.TrustManagerSslContext;
import de.adito.trustmanager.store.JKSCustomTrustStore;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Updates the jloadr before starting it
 */
public class Starter
{
  private static final String JLOADR_JAR = "jloadr.jar";
  private static final String JLOADR_JAR_SHA1 = "jloadr.jar.sha1";

  public static void main(String[] args) throws Throwable
  {
    if (args.length == 0)
      throw new RuntimeException("first parameter must be the repository url");

    String minimalVersion = System.getProperty("min.java");
    if(!(VersionUtil.validateJavaVersion(System.getProperty("java.version"), minimalVersion)))
    {
      String msg = "Your Java is outdated, please use at least Java " +
          (minimalVersion == null ? VersionUtil.getDefaultVersion() : minimalVersion);
      ShowErrorUtil.showStartError(msg);
      throw new RuntimeException(msg);
    }

    _initTrustManager();

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
      new ShowErrorUtil(loadError, pE).showError();
      throw loadError == null ? pE : loadError;
    }
  }

  /**
   * Loads the newest jloadr Version from the connected server
   * @param pUrl server url
   * @throws IOException
   */
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

  /**
   * Starts the actual jloadr after it was updated
   * @param pLocalJar jloadr.jar
   * @param pClassName main class of jloadr
   * @param pArgs arguments for the jloadr, eg. the server url
   * @throws Throwable
   */
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
   * Initialises a trustManager to validate certificates and starts a SSLContext
   */
  private static void _initTrustManager() throws CertificateException, NoSuchAlgorithmException, KeyStoreException,
      KeyManagementException, InvalidAlgorithmParameterException, IOException
  {
    Path trustStorePath = Paths.get(JKSCustomTrustStore.TRUST_STORE_PATH).toAbsolutePath();
    System.setProperty(JKSCustomTrustStore.TURST_STORE_PATH_SYSTEM_PROPERTY, trustStorePath.toString());
    System.setProperty("jloadr." + JKSCustomTrustStore.TURST_STORE_PATH_SYSTEM_PROPERTY, trustStorePath.toString());
    TrustManagerSslContext.initSslContext();
  }

}
