package de.adito.jloadr.local;

import de.adito.jloadr.api.*;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.stream.*;

/**
 * @author j.boesl, 05.09.16
 */
public class LocalStore implements IStore
{
  private ScheduledExecutorService executor;
  private Path directory;
  private Map<String, LocalStoreResourcePack> resourcePackMap;

  public LocalStore(ScheduledExecutorService pExecutor)
  {
    executor = pExecutor;
    //Paths.get(System.getProperty("user.home"), "jloadr")
    directory = Paths.get("jloadr");
    try {
      Files.createDirectories(directory);
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
    try {
      try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory, entry -> Files.isDirectory(entry))) {
        resourcePackMap = StreamSupport.stream(dirStream.spliterator(), false)
            .map(dir -> new LocalStoreResourcePack(executor, dir))
            .collect(Collectors.toMap(LocalStoreResourcePack::getId, Function.identity()));
      }
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  @Nonnull
  @Override
  public Set<String> getResourcePackIds()
  {
    return resourcePackMap.keySet();
  }

  @Override
  public boolean containsResourcePack(@Nonnull String pId)
  {
    return resourcePackMap.containsKey(pId);
  }

  @Nonnull
  @Override
  public IStoreResourcePack getResourcePack(@Nonnull String pId)
  {
    LocalStoreResourcePack localStoreResourcePack = resourcePackMap.get(pId);
    if (localStoreResourcePack != null)
      return localStoreResourcePack;
    throw new RuntimeException("resourcePack '" + pId + "' does not exist at '" + this + "'.");
  }

  @Override
  public IStoreResourcePack addResourcePack(@Nonnull String pId)
  {
    try {
      Path created = Files.createDirectories(directory.resolve(pId));
      LocalStoreResourcePack localStoreResourcePack = new LocalStoreResourcePack(executor, created);
      resourcePackMap.put(localStoreResourcePack.getId(), localStoreResourcePack);
      return localStoreResourcePack;
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void removeResourcePack(@Nonnull String pId)
  {
    LocalStoreResourcePack removed = resourcePackMap.remove(pId);
    if (removed == null)
      return;

    Path path = directory.resolve(pId);
    try {
      Files.walkFileTree(path, new SimpleFileVisitor<Path>()
      {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
        {
          Files.delete(file);
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
        {
          Files.delete(dir);
          return FileVisitResult.CONTINUE;
        }

      });
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

}
