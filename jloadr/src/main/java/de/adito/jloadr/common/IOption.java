package de.adito.jloadr.common;

import java.util.Optional;

public interface IOption
{

  /**
   * The time jloadr shall wait for the main application to start.
   */
  int WAIT_FOR_START = Optional.ofNullable(System.getProperty("jlr.waitForStart"))
      .map(property -> {
        try
        {
          return Integer.parseInt(property);
        }
        catch (NumberFormatException pE)
        {
          return null;
        }
      }).orElse(4);

  /**
   * Tell jloadr to stay active and pass on console output
   */
  boolean KEEP_ATTACHED = System.getProperty("jlr.keepAttached") != null;

}
