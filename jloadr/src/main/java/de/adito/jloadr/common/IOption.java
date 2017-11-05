package de.adito.jloadr.common;

import java.util.function.Supplier;

public interface IOption
{

  /**
   * The time jloadr shall wait for the main application to start.
   */
  int WAIT_FOR_START = ((Supplier<Integer>) () -> {
    String property = System.getProperty("jloadr.waitForStart");
    if (property != null)
    {
      try
      {
        return Integer.parseInt(property);
      }
      catch (NumberFormatException pE)
      {
        // ignore
      }
    }
    return 4;
  }).get();

}
