package de.adito.jloadr.api;

import java.io.*;

/**
 * @author j.boesl, 05.09.16
 */
public interface IResource
{

  IResourceId getId();

  InputStream getInputStream() throws IOException;

  long getSize() throws IOException;

  long getLastModified() throws IOException;

  String getHash();

}
