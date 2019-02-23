package de.adito.jloadr.common;

import java.io.*;
import java.nio.file.*;

/**
 * A class to fetch run and java commands
 * @author j.boesl, 26.01.17
 */
public class ProcessUtil
{

  public static String runCmd(String... pCmd) throws IOException
  {
    Process process = new ProcessBuilder(pCmd)
        .start();

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    JLoadrUtil.copy(process.getInputStream(), byteArrayOutputStream);
    return new String(byteArrayOutputStream.toByteArray());
  }

  public static String findJavaCmd(Path pWorkingDirectory, String pProposal)
  {
    Path javaBinaryPath = null;

    if (pProposal != null)
      javaBinaryPath = _getJavaBinary(pWorkingDirectory == null ? Paths.get(pProposal) : pWorkingDirectory.resolve(pProposal));

    if (javaBinaryPath == null && pWorkingDirectory != null)
      javaBinaryPath = _getJavaBinary(pWorkingDirectory.resolve("jre/bin"));

    if (javaBinaryPath == null)
      return "java";

    return javaBinaryPath.toAbsolutePath().toString();
  }

  private static Path _getJavaBinary(Path pMaybeJrePath)
  {
    if (Files.isDirectory(pMaybeJrePath))
    {
      Path javaBinaryPath = pMaybeJrePath.resolve(OsUtil.getOsType().equals(OsUtil.EType.WINDOWS) ? "java.exe" : "java");
      if (Files.isRegularFile(javaBinaryPath))
      {
        if (!Files.isExecutable(javaBinaryPath))
          javaBinaryPath.toFile().setExecutable(true);
        return javaBinaryPath;
      }
    }
    return null;
  }

}
