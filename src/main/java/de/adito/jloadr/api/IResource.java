package de.adito.jloadr.api;

import javax.annotation.*;
import java.io.*;

/**
 * @author j.boesl, 05.09.16
 */
public interface IResource
{

  @Nonnull
  IResourceId getId();

  @Nonnull
  InputStream getInputStream() throws IOException;

  long getSize() throws IOException;

  long getLastModified() throws IOException;

  @Nullable
  String getHash();

}
