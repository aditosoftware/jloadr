package de.adito.jloadr.repository.local;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author j.boesl, 08.09.16
 */
public class LocalStoreProperties
{

  private static final String FILE_NAME = "store.properties";

  private final Properties properties;
  private final Path filePath;
  private final ScheduledExecutorService executor;
  private Runnable task;

  public LocalStoreProperties(ScheduledExecutorService pExecutor, Path pPath)
  {
    assert Files.isDirectory(pPath);
    properties = new Properties();
    filePath = pPath.resolve(FILE_NAME);
    try {
      if (Files.isRegularFile(filePath))
        properties.load(Files.newBufferedReader(filePath));
      else
        Files.createFile(filePath);
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
    executor = pExecutor;
  }

  public String get(String key)
  {
    return properties.getProperty(key);
  }

  public String get(String key, String defaultValue)
  {
    return properties.getProperty(key, defaultValue);
  }

  public boolean isEmpty()
  {
    return properties.isEmpty();
  }

  public boolean containsValue(String value)
  {
    return properties.containsValue(value);
  }

  public boolean containsKey(String key)
  {
    return properties.containsKey(key);
  }

  public String put(String key, String value)
  {
    Object put = properties.put(key, value);
    _requestWriteBack();
    return (String) put;
  }

  public String remove(String key)
  {
    Object remove = properties.remove(key);
    _requestWriteBack();
    return (String) remove;
  }

  public void clear()
  {
    properties.clear();
    _requestWriteBack();
  }

  public Set<String> keySet()
  {
    return (Set) properties.keySet();
  }

  public Set<Map.Entry<String, String>> entrySet()
  {
    return (Set) properties.entrySet();
  }

  public Collection<String> values()
  {
    return (Collection) properties.values();
  }

  private synchronized void _requestWriteBack()
  {
    task = new Runnable()
    {
      @Override
      public void run()
      {
        if (task == this)
          try {
            properties.store(Files.newBufferedWriter(filePath), "");
          }
          catch (IOException pE) {
            pE.printStackTrace();
          }
      }
    };
    executor.schedule(task, 200, TimeUnit.MILLISECONDS);
  }

}
