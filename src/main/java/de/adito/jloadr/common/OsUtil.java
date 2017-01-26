package de.adito.jloadr.common;

import java.util.Objects;

/**
 * @author j.boesl, 26.01.17
 */
public class OsUtil
{

  public static EType getOsType()
  {
    String osName = Objects.toString(getOsName(), "").toLowerCase();
    if (osName.contains("win"))
      return EType.WINDOWS;
    if (osName.contains("mac"))
      return EType.OSX;
    if (osName.contains("nix") || osName.contains("nux") || osName.indexOf("aix") > 0)
      return EType.LINUX;
    if (osName.contains("sunos"))
      return EType.SOLARIS;
    return EType.UNKNWON;
  }

  public static EBitness getBitness()
  {
    String arch = Objects.toString(System.getProperty("sun.arch.data.model"), "");
    if (arch.contains("64"))
      return EBitness.X64;
    if (arch.contains("32"))
      return EBitness.X32;

    arch = Objects.toString(System.getProperty("os.arch"), "");
    if (arch.contains("64"))
      return EBitness.X64;
    return EBitness.X32;
  }

  static String getOsName()
  {
    return System.getProperty("os.name");
  }

  /**
   * Definition of os types.
   */
  public enum EType
  {
    /**
     * Microsoft Windows
     */
    WINDOWS,
    /**
     * Linux
     */
    LINUX,
    /**
     * Mac OS X
     */
    OSX,
    /**
     * Oracle Solaris
     */
    SOLARIS,
    /**
     * Unknown operating system
     */
    UNKNWON
  }

  /**
   * Bitness of operation system.
   */
  public enum EBitness
  {

    /**
     * 32-bit
     */
    X32,
    /**
     * 64-bit
     */
    X64

  }
}
