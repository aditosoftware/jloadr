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

  /**
   * Liefert den Haswert der Datei zur Überprüfung auf Dateiänderungen
   * @return Hashwert
   */
  String getHash();

}
