package de.adito.jloadr.repository.local;

import de.adito.jloadr.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

/**
 * @author j.boesl, 05.09.16
 */
public class LocalStore implements IStore
{
  private Path directory;
  private Map<String, LocalStoreResourcePack> resourcePackMap;

  public LocalStore(Path pStoreDirectory)
  {
    directory = pStoreDirectory;
    try {
      Files.createDirectories(directory);
      try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory, entry -> Files.isDirectory(entry))) {
        resourcePackMap = StreamSupport.stream(dirStream.spliterator(), false)
            .map(dir -> new LocalStoreResourcePack(dir, getConfigPathForDirectory(dir)))
            .collect(Collectors.toMap(LocalStoreResourcePack::getId, Function.identity()));
      }
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public Set<String> getResourcePackIds()
  {
    return resourcePackMap.keySet();
  }

  @Override
  public boolean containsResourcePack(String pId)
  {
    return resourcePackMap.containsKey(pId);
  }

  @Override
  public IStoreResourcePack getResourcePack(String pId)
  {
    LocalStoreResourcePack localStoreResourcePack = resourcePackMap.get(pId);
    if (localStoreResourcePack != null)
      return localStoreResourcePack;
    throw new RuntimeException("resourcePack '" + pId + "' does not exist at '" + this + "'.");
  }

  @Override
  public IStoreResourcePack addResourcePack(String pId)
  {
    try {
      Path created = Files.createDirectories(directory.resolve(pId));
      LocalStoreResourcePack localStoreResourcePack = new LocalStoreResourcePack(created, getConfigPathForDirectory(created));
      resourcePackMap.put(localStoreResourcePack.getId(), localStoreResourcePack);
      return localStoreResourcePack;
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public void removeResourcePack(String pId)
  {
    LocalStoreResourcePack removed = resourcePackMap.remove(pId);
    if (removed == null)
      return;

    Path path = directory.resolve(pId);
    try {
      Files.deleteIfExists(getConfigPathForDirectory(directory));
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

  private static Path getConfigPathForDirectory(Path pPath)
  {
    return pPath.getParent().resolve(pPath.getFileName() + ".jlr.xml");
  }

}
