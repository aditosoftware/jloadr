package de.adito.jloadr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.repository.jnlp.JnlpStartConfig;
import de.adito.jloadr.repository.local.LocalStore;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 05.09.16
 */
public class Main
{

  public static void main(String[] args) throws IOException, InterruptedException
  {
    JnlpStartConfig startConfig = new JnlpStartConfig(new URL(args[0]));
    Splash splash = GraphicsEnvironment.isHeadless() ? null : new Splash(startConfig.getSplashURL());

    try {
      IResourcePack resourcePack = startConfig.getResourcePack();
      //Paths.get(System.getProperty("user.home"), "jloadr")
      LocalStore localStore = new LocalStore(Paths.get("jloadr"));

      new Loader().load(localStore, resourcePack, new ILoader.IStateCallback()
      {
        private int elementCount;

        @Override
        public void inited(int pElementCount)
        {
          elementCount = pElementCount;
        }

        @Override
        public void loaded(int pElementNumber)
        {
          if (splash != null)
            splash.setPercentage((double) pElementNumber / (double) elementCount);
        }

        @Override
        public void finished()
        {
          if (splash != null)
            splash.setPercentage(1);
        }
      });

      IStoreResourcePack localResourcePack = localStore.getResourcePack(resourcePack.getId());
      localResourcePack.writeConfig();
      //List<IStoreResource> resources = localResourcePack.getResources();
      //for (IResource resource : resources) {
      //  System.out.println("locally found: " + resource);
      //}

      System.out.println(Arrays.stream(startConfig.getStartCommand()).collect(Collectors.joining(" ")));
      Process process = Runtime.getRuntime().exec(startConfig.getStartCommand(), null, new File("jloadr", localResourcePack.getId()));
      //System.err.println(process.waitFor());
      //print(process.getInputStream());
      Thread.sleep(3000);
    }
    finally {
      if (splash != null)
        SwingUtilities.invokeLater(splash::dispose);
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


  private static class Splash extends JWindow
  {
    private double loaded;

    public Splash(URL pURL)
    {
      getContentPane().add(
          pURL == null ?
              new JLabel("loading ...", SwingConstants.CENTER) :
              new JLabel("", new ImageIcon(pURL), SwingConstants.CENTER));

      pack();
      setVisible(true);
    }

    public void setPercentage(double pLoaded)
    {
      SwingUtilities.invokeLater(() -> {
        loaded = pLoaded;
        revalidate();
        repaint();
      });
    }

    @Override
    public void paint(Graphics g)
    {
      super.paint(g);
      int w = getContentPane().getWidth();
      int h = getContentPane().getHeight();
      g.setColor(SystemColor.GRAY);
      g.fillRect(4, h - 8, (int) Math.round((w - 8) * loaded), 4);
    }
  }

}
