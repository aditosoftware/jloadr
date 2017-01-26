package de.adito.jloadr.common;

import java.io.*;

/**
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

}
