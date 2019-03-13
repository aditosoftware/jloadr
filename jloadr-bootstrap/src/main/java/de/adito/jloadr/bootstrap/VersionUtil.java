package de.adito.jloadr.bootstrap;

import java.io.File;

/**
 * @author m.schindlbeck, 12.03.19
 */
class VersionUtil
{
  private static final String DEFAULT_MIN_JAVA_VERSION = "9";

  /**
   * Prevent instantiation
   */
  private VersionUtil()
  {
  }

  /**
   * The versions will be compared to determine that pCurrentVersion is at least pMinimalVersion. The check will be
   * ignored if a 'jre' folder is included in the working directory.
   */
  static boolean validateJavaVersion(String pCurrentVersion, String pMinimalVersion)
  {
    String minimalVersion = _validateVersionFormat(pMinimalVersion);
    String currentVersion = _validateVersionFormat(pCurrentVersion);
    return _isExpectedJavaVersion(currentVersion, minimalVersion);
  }

  /**
   * Checks if the VM Java version is at least the minimum version. It only uses the first two instances
   * of the version number to compare the version. If the folder 'jre' exists, the check will be ignored.
   */

  private static boolean _isExpectedJavaVersion(String pCurrent, String pMinimal)
  {
    if(new File(System.getProperty("user.dir") + File.separator + "jre").exists())
      return true;

    String[] minimalArray = pMinimal.split("\\.", 3);
    String[] currentArray = pCurrent.split("\\.", 3);

    int current = (Integer.parseInt(currentArray[0]) * 10) +
        (currentArray.length < 2 ? 0 : Integer.parseInt(currentArray[1]));

    int minimal = (Integer.parseInt(minimalArray[0]) * 10) +
        (minimalArray.length < 2 ? 0 : Integer.parseInt(minimalArray[1]));

    if(current < minimal)
      return false;

    return true;
  }

  /**
   * Validates the given version number. Numbers that start with '-' or a number between '2' and '8' will
   * throw a RuntimeException. Versions like '1.8', '10', '10.2' etc are expected.
   * @return a String with a valid version number
   */
  private static String _validateVersionFormat(String pVersion)
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

  static String getDefaultVersion()
  {
    return DEFAULT_MIN_JAVA_VERSION;
  }

}
