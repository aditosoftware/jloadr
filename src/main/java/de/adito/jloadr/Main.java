package de.adito.jloadr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.repository.jlr.JlrResourcePack;
import de.adito.jloadr.repository.jnlp.JnlpResourcePack;
import de.adito.jloadr.repository.local.LocalStore;

import javax.imageio.ImageIO;
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
    IResourcePack remoteResourcePack;
    URL url = new URL(args[0]);
    if (url.getPath().endsWith("jnlp"))
      remoteResourcePack = new JnlpResourcePack(url);
    else if (url.getPath().endsWith("jlr.xml"))
      remoteResourcePack = new JlrResourcePack(url);
    else
      throw new RuntimeException("resource not supported: " + url.toExternalForm());

    Splash splash = GraphicsEnvironment.isHeadless() ? null : new Splash();

    try {
      //Paths.get(System.getProperty("user.home"), "jloadr")
      LocalStore localStore = new LocalStore(Paths.get("jloadr"));

      new Loader().load(localStore, remoteResourcePack, splash);

      IStoreResourcePack localResourcePack = localStore.getResourcePack(remoteResourcePack.getId());
      localResourcePack.writeConfig();
      //List<IStoreResource> resources = localResourcePack.getResources();
      //for (IResource resource : resources) {
      //  System.out.println("locally found: " + resource);
      //}

      IStoreResource configResource = localResourcePack.getResource(JLoaderConfig.CONFIG_NAME);
      if (configResource != null) {
        JLoaderConfig loaderConfig = new JLoaderConfig();
        try (InputStream inputStream = configResource.getInputStream()) {
          loaderConfig.load(inputStream);
        }

        System.out.println(Arrays.stream(loaderConfig.getStartCommands()).collect(Collectors.joining(" ")));

        Process process = Runtime.getRuntime().exec(loaderConfig.getStartCommands(), null,
                                                    new File("jloadr", localResourcePack.getId()));

        //System.err.println(process.waitFor());
        print(process.getInputStream());
      }

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


  private static class Splash extends JWindow implements ILoader.IStateCallback
  {
    private int elementCount;
    private double loaded;


    @Override
    public void inited(IResource pSplashResource, int pElementCount)
    {
      elementCount = pElementCount;

      JLabel label = null;
      if (pSplashResource != null) {
        try {
          label = new JLabel("", new ImageIcon(ImageIO.read(pSplashResource.getInputStream())), SwingConstants.CENTER);
        }
        catch (IOException pE) {
          // no image
        }
      }
      if (label == null)
        label = new JLabel("loading ...", SwingConstants.CENTER);

      getContentPane().add(label);

      pack();
      setVisible(true);
    }

    @Override
    public void loaded(int pElementNumber)
    {
      setPercentage((double) pElementNumber / (double) elementCount);
    }

    @Override
    public void finished()
    {
      setPercentage(1);
    }

    private void setPercentage(double pLoaded)
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
