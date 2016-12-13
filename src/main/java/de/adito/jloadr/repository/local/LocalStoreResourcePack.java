package de.adito.jloadr.repository.local;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 05.09.16
 */
public class LocalStoreResourcePack implements IStoreResourcePack
{
  private static final String DELIMITER = "; ";

  private Path root;
  private LocalStoreProperties properties;
  private Set<String> resources;

  public LocalStoreResourcePack(ScheduledExecutorService pExecutor, Path pRoot)
  {
    root = pRoot;
    properties = new LocalStoreProperties(pExecutor, pRoot);

    String r = properties.get("resources");
    resources = new LinkedHashSet<>(r == null ? Collections.emptyList() : Arrays.asList(properties.get("resources").split(DELIMITER)));
  }

  @Nonnull
  @Override
  public String getId()
  {
    return root.getFileName().toString();
  }

  @Nonnull
  @Override
  public List<IStoreResource> getResources()
  {
    return resources.stream()
        .map(str -> {
          Path path = root.resolve(str);
          return new LocalStoreResource(str, path);
        })
        .collect(Collectors.toList());
  }

  @Override
  public IStoreResource getResource(@Nonnull String pId)
  {
    Path path = root.resolve(pId);
    return Files.isRegularFile(path) ? new LocalStoreResource(pId, path) : null;
  }

  @Nonnull
  @Override
  public synchronized IStoreResource createResource(@Nonnull String pId)
  {
    Path path = root.resolve(pId);
    if (Files.exists(path))
      throw new RuntimeException("Resource already exists: " + path);
    try {
      Files.createDirectories(path.getParent());
      LocalStoreResource resource = new LocalStoreResource(pId, Files.createFile(path));
      resources.add(resource.getId());
      properties.put("resources", resources.stream().collect(Collectors.joining(DELIMITER)));
      return resource;
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public synchronized void removeResource(@Nonnull String pId)
  {
    Path path = root.resolve(pId);
    if (Files.isRegularFile(path)) {
      try {
        Files.delete(path);
        resources.remove(pId);
        properties.put("resources", resources.stream().collect(Collectors.joining(DELIMITER)));
      }
      catch (IOException pE) {
        throw new RuntimeException(pE);
      }
    }
  }

  @Override
  public boolean equals(Object pO)
  {
    if (this == pO)
      return true;
    if (pO == null || getClass() != pO.getClass())
      return false;
    LocalStoreResourcePack that = (LocalStoreResourcePack) pO;
    return Objects.equals(root, that.root);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(root);
  }

  @Override
  public String toString()
  {
    return JLoadrUtil.toSimpleInfo(this, root.toString());
  }

}
