package de.adito.jloadr.api;

import java.io.*;

/**
 * @author j.boesl, 08.09.16
 */
public interface IStoreResource extends IResource
{

  OutputStream getOutputStream() throws IOException;

  void setLastModified(long pTime);

  void setHash(String pHash);

}
