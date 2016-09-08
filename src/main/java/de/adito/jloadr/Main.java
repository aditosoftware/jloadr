package de.adito.jloadr;

import de.adito.jloadr.api.IStoreResourcePack;
import de.adito.jloadr.jnlp.JnlpResourcePack;
import de.adito.jloadr.local.LocalStore;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 05.09.16
 */
public class Main
{

  public static void main(String[] args) throws IOException, InterruptedException
  {
    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    try {
      JnlpResourcePack resourcePack = new JnlpResourcePack(new URL(args[0]));
      LocalStore localStore = new LocalStore(executor);

      new Loader().load(localStore, resourcePack);

      IStoreResourcePack localResourcePack = localStore.getResourcePack(resourcePack.getId());
      //List<IStoreResource> resources = localResourcePack.getResources();
      //for (IResource resource : resources) {
      //  System.out.println("locally found: " + resource);
      //}

      System.out.println(Arrays.stream(resourcePack.getStartCommand()).collect(Collectors.joining(" ")));
      Process process = Runtime.getRuntime().exec(resourcePack.getStartCommand(), null, new File("jloadr", localResourcePack.getId()));
      //System.err.println(process.waitFor());
      //print(process.getInputStream());
    }
    finally {
      executor.shutdown();
    }
  }

  public static void print(InputStream pIn) throws IOException
  {
    try (OutputStream out = System.out; InputStream in = pIn) {
      byte[] buffer = new byte[256 * 1024];
      int len;
      while ((len = in.read(buffer)) != -1)
        out.write(buffer, 0, len);
    }
  }

}
