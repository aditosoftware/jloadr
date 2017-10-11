package de.adito.jloadr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;
import de.adito.jloadr.repository.*;
import de.adito.jloadr.repository.local.LocalStore;
import org.w3c.dom.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author j.boesl, 05.09.16
 */
public class Main
{

  public static void main(String[] args) throws IOException, InterruptedException
  {

    String url = null;
    String iconPath = null;
    String startName = null;
    if (args.length > 0)
      url = args[0];
    else
    {
      Path configPath = Paths.get("config.xml");
      if (Files.exists(configPath))
      {
        Document document = XMLUtil.loadDocument(configPath.toUri().toURL());
        Element documentElement = document.getDocumentElement();
        url = XMLUtil.getChildText(documentElement, "url");
        iconPath = XMLUtil.getChildText(documentElement, "icon");
        startName = XMLUtil.getChildText(documentElement, "name");
      }
    }
    if (url == null)
      throw new RuntimeException("a repository url must be specified.");

    IResourcePack remoteResourcePack = ResourcePackFactory.get(new URL(url));

    Splash splash = GraphicsEnvironment.isHeadless() ? null : new Splash(iconPath, startName);

    try
    {
      LocalStore localStore = new LocalStore(Paths.get("jloadr"));

      IStoreResourcePack localResourcePack = new Loader().load(localStore, remoteResourcePack, splash);

      IStoreResource configResource = localResourcePack.getResource(JLoaderConfig.CONFIG_ID);
      if (configResource != null)
      {
        JLoaderConfig loaderConfig = new JLoaderConfig();
        try (InputStream inputStream = configResource.getInputStream())
        {
          loaderConfig.load(inputStream);
        }

        Path workingDirectory = Paths.get("jloadr").resolve(localResourcePack.getId()).toAbsolutePath();
        if (Arrays.asList(OsUtil.EType.LINUX, OsUtil.EType.OSX).contains(OsUtil.getOsType()))
        {
          String javaCmd = loaderConfig.getJavaCmd();
          if (javaCmd != null && Files.exists(workingDirectory.resolve(javaCmd)))
          {
            Process chmodProcess = new ProcessBuilder("chmod", "+x", javaCmd)
                .directory(workingDirectory.toFile())
                .start();
            chmodProcess.waitFor(1, TimeUnit.SECONDS);
          }
        }
        Process javaProcess = new ProcessBuilder(loaderConfig.getStartCommands(workingDirectory))
            .directory(workingDirectory.toFile())
            .inheritIO()
            .start();
        javaProcess.waitFor(4, TimeUnit.SECONDS);
      }
    }
    finally
    {
      if (splash != null)
        SwingUtilities.invokeLater(splash::dispose);
    }
  }


  private static class Splash extends JFrame implements ILoader.IStateCallback
  {
    private int elementCount;
    private double loaded;

    public Splash(String pIconPath, String pStartName) throws HeadlessException
    {
      if (pIconPath != null)
      {
        Path path = Paths.get(pIconPath);
        if (Files.exists(path))
          setIconImage(new ImageIcon(path.toString()).getImage());
      }
      if (pStartName != null)
        setTitle(pStartName);
    }

    @Override
    public void inited(IResource pSplashResource, int pElementCount)
    {
      elementCount = pElementCount;

      JLabel label = null;
      if (pSplashResource != null)
      {
        try
        {
          BufferedImage image = ImageIO.read(pSplashResource.getInputStream());
          label = new JLabel("", new ImageIcon(image), SwingConstants.CENTER);
        }
        catch (IOException pE)
        {
          // no image
        }
      }
      if (label == null)
        label = new JLabel("loading ...", SwingConstants.CENTER);

      getContentPane().add(label);

      setUndecorated(true);
      pack();
      setLocationRelativeTo(null);
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
