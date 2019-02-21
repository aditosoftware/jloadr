package de.adito.jloadr;

import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;
import de.adito.jloadr.gui.Splash;
import de.adito.jloadr.repository.*;
import de.adito.jloadr.repository.local.LocalStore;
import org.w3c.dom.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
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

    Path configPath = Paths.get("config.xml");
    if (Files.exists(configPath))
    {
      Document document = XMLUtil.loadDocument(configPath.toUri().toURL());
      Element documentElement = document.getDocumentElement();
      if (url == null)
        url = XMLUtil.getChildText(documentElement, "url");

      iconPath = XMLUtil.getChildText(documentElement, "icon");
      startName = XMLUtil.getChildText(documentElement, "name");
    }
    _run(url, iconPath, startName);
  }

  private static void _run(String pUrl, String pIconPath, String pStartName) throws IOException, InterruptedException
  {

    if (pUrl == null)
      throw new RuntimeException("a repository pUrl must be specified.");

    IResourcePack remoteResourcePack = ResourcePackFactory.get(new URL(pUrl));

    Splash splash = GraphicsEnvironment.isHeadless() ? null : new Splash(pIconPath, pStartName);

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

        //client wird ausgeführt
        Path workingDirectory = Paths.get("jloadr").resolve(localResourcePack.getId()).toAbsolutePath();
        String[] command = loaderConfig.getStartCommands(workingDirectory, JLoadrUtil.getAdditionalSystemParameters());
        Process javaProcess  = new ProcessBuilder(command)
            .directory(workingDirectory.toFile())
            .inheritIO()
            .start();
        javaProcess.waitFor(IOption.WAIT_FOR_START, TimeUnit.SECONDS);

        if (!javaProcess.isAlive() && javaProcess.exitValue() != 0)
          throw new RuntimeException("application exited with exit code " + javaProcess.exitValue());

        if (splash != null)
        {
          splash.dispose();
          splash = null;
        }

        if (IOption.KEEP_ATTACHED)
          System.exit(javaProcess.waitFor());
      }
    }
    finally
    {
      if (splash != null)
        SwingUtilities.invokeLater(splash::dispose);
    }
  }
}
