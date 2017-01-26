package de.adito.jloadr.repository.local;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.JLoadrUtil;
import de.adito.jloadr.repository.ResourceId;
import de.adito.jloadr.repository.jlr.*;

import javax.annotation.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author j.boesl, 05.09.16
 */
public class LocalStoreResourcePack implements IStoreResourcePack
{
  private Path root;
  private Map<IResourceId, IStoreResource> resourceMap;
  private JlrPack jlrPack;

  public LocalStoreResourcePack(Path pRoot, Path pConfigPath)
  {
    root = pRoot;
    resourceMap = new HashMap<>();
    try {
      if (!Files.exists(pConfigPath))
        Files.createFile(pConfigPath);
      jlrPack = new JlrPack(pConfigPath.toUri().toURL());

      Files.walkFileTree(pRoot, new SimpleFileVisitor<Path>()
      {
        private JlrPack loadedPack = new JlrPack(pConfigPath.toUri().toURL())
        {{
          loadPack();
        }};

        @Override
        public FileVisitResult visitFile(Path pPath, BasicFileAttributes pAttrs) throws IOException
        {
          IResourceId id = new ResourceId(root.relativize(pPath));
          JlrEntry entry = loadedPack.getEntry(id);
          if (entry == null)
            entry = new JlrEntry(id);
          jlrPack.addEntry(entry);
          LocalStoreResource resource = new LocalStoreResource(entry, pPath);
          resourceMap.put(id, resource);
          return FileVisitResult.CONTINUE;
        }
      });
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
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
    return new ArrayList<>(resourceMap.values());
  }

  @Nullable
  @Override
  public IStoreResource getResource(@Nonnull IResourceId pId)
  {
    return resourceMap.get(pId);
  }

  @Nonnull
  @Override
  public synchronized IStoreResource createResource(@Nonnull IResourceId pId)
  {
    Path path = root.resolve(pId.toPath());
    if (Files.exists(path))
      throw new RuntimeException("Resource already exists: " + path);
    try {
      Files.createDirectories(path.getParent());
      JlrEntry jlrEntry = new JlrEntry(pId);
      LocalStoreResource resource = new LocalStoreResource(jlrEntry, Files.createFile(path));
      jlrPack.addEntry(jlrEntry);
      resourceMap.put(pId, resource);
      return resource;
    }
    catch (IOException pE) {
      throw new RuntimeException(pE);
    }
  }

  @Override
  public synchronized void removeResource(@Nonnull IResourceId pId)
  {
    Path path = root.resolve(pId.toPath());
    if (Files.isRegularFile(path)) {
      try {
        Files.delete(path);
        resourceMap.remove(pId);
        jlrPack.removeEntry(pId);
        JLoadrUtil.deleteEmptyDirectories(path.getParent());
      }
      catch (IOException pE) {
        throw new RuntimeException(pE);
      }
    }
  }

  @Override
  public void writeConfig()
  {
    jlrPack.writePack();
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
