package de.adito.jloadr.clientstarter;

import de.adito.jloadr.Loader;
import de.adito.jloadr.api.*;
import de.adito.jloadr.common.*;
import de.adito.jloadr.gui.Splash;
import de.adito.jloadr.repository.*;
import de.adito.jloadr.repository.local.LocalStore;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/**
 * @author m.schindlbeck, 05.05.2020
 */
public class ClientStarter
{
  private final String url;
  private final String iconPath;
  private final String startName;
  private Path workingDirectory = null;
  private String[] commands = null;
  private boolean useJava;

  public ClientStarter(String pUrl, String pIconPath, String pStartName)
  {
    this.url = pUrl;
    this.iconPath = pIconPath;
    this.startName = pStartName;
    this.useJava = true;
  }

  public void start() throws IOException, InterruptedException
  {
    if(url == null)
      throw new RuntimeException("a repository url must be specified.");

    IResourcePack remoteResourcePack = ResourcePackFactory.get(new URL(url));

    Splash splash = GraphicsEnvironment.isHeadless() ? null : new Splash(iconPath,startName);

    try
    {
      LocalStore localStore = new LocalStore(Paths.get("jloadr"));

      //should be the actual loading part
      IStoreResourcePack localResourcePack = new Loader().load(localStore,remoteResourcePack,splash);

      //todo add switch case here and make several functions
      //todo fallback to javaclient
      _loadConfig(localResourcePack);
      Process clientProcess = _startClientProcess();

      //show a loading picture
      if(splash!=null)
      {
        splash.dispose();
        splash=null;
      }

      if(IOption.KEEP_ATTACHED)
        System.exit(clientProcess.waitFor());
      }

    finally
    {
      if(splash!=null)
        SwingUtilities.invokeLater(splash::dispose);
    }
  }

  private void _loadConfig(IStoreResourcePack pLocalResourcePack) throws IOException
  {
    IStoreResource configResource = pLocalResourcePack.getResource(JLoaderConfig.CONFIG_ID);
    if (configResource != null)
    {
      JLoaderConfig loaderConfig = new JLoaderConfig();
      try (InputStream inputStream = configResource.getInputStream())
      {
        loaderConfig.load(inputStream);
      }

      workingDirectory = Paths.get("jloadr").resolve(pLocalResourcePack.getId()).toAbsolutePath();

      String useJavaClient = loaderConfig.getUseJavaClient();
      if(useJavaClient == null || useJavaClient.contentEquals("true"))
      {
        useJava = true;
        commands = loaderConfig.getStartCommands(workingDirectory, JLoadrUtil.getAdditionalSystemParameters());
      }
      else
      {
        useJava = false;
        commands = loaderConfig.getExecStartCommands(workingDirectory);

      }
    }
  }

  private Process _startClientProcess() throws IOException, InterruptedException
  {
    Process process;
    if(useJava)
    {
      process = new ProcessBuilder(commands)
          .directory(workingDirectory.toFile())
          .inheritIO()
          .start();
    }
    else
    {
      process = new ProcessBuilder(commands)
          .inheritIO()
          .start();
    }

    process.waitFor(IOption.WAIT_FOR_START,TimeUnit.SECONDS);

    if(!process.isAlive() && process.exitValue()!=0)
      throw new RuntimeException("application exited with exit code "+ process.exitValue());

    return process;
  }
}


