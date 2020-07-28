package de.adito.jloadr;

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
 * Loads clients provided by the server and chooses the correct ProcessBuilder to start the loaded application with its configurations.
 * @author m.schindlbeck, 05.05.2020
 */
public class ClientStarter
{
  private final String url;
  private final String iconPath;
  private final String startName;
  private Path workingDirectory = null;
  private String[] commands = null;

  public ClientStarter(String pUrl, String pIconPath, String pStartName)
  {
    this.url = pUrl;
    this.iconPath = pIconPath;
    this.startName = pStartName;
  }

  /**
   * Loads the client and the splash image for showing the loading progress
   * @throws IOException when the loading part fails
   * @throws InterruptedException when starting the client fails
   */
  public void start() throws IOException, InterruptedException
  {
    if(url == null)
      throw new RuntimeException("The server url is missing. Please ask your administrator for help.");

    IResourcePack remoteResourcePack = ResourcePackFactory.get(new URL(url));

    Splash splash = GraphicsEnvironment.isHeadless() ? null : new Splash(iconPath,startName);

    try
    {
      LocalStore localStore = new LocalStore(Paths.get("jloadr"));

      //actual loading part
      IStoreResourcePack localResourcePack = new Loader().load(localStore,remoteResourcePack,splash);

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

  /**
   * Loads the jLoadrConfig and prepares additional start commands for the processBuilder if providedand according to the
   * specified <clientType>.
   * @return the loaded config
   * @throws IOException
   */
  private JLoaderConfig _loadConfig(IStoreResourcePack pLocalResourcePack) throws IOException
  {
    String clientType = null;
    IStoreResource configResource = pLocalResourcePack.getResource(JLoaderConfig.CONFIG_ID);
    JLoaderConfig loaderConfig = new JLoaderConfig();

    if (configResource != null)
    {

      try (InputStream inputStream = configResource.getInputStream())
      {
        loaderConfig.loadConfigTags(inputStream);
      }

      workingDirectory = Paths.get("jloadr").resolve(pLocalResourcePack.getId()).toAbsolutePath();

      clientType = loaderConfig.getClientType();
      if (clientType.isEmpty())
        clientType = "java";

      switch (clientType.toLowerCase())
      {
        case "electron":
          commands = loaderConfig.getElectronStartCommands(workingDirectory);
          break;

        case "java":
          commands = loaderConfig.getStartCommands(workingDirectory, JLoadrUtil.getAdditionalSystemParameters());
          break;

        default:
          throw new RuntimeException("No valid client for starting found. Check the tag <clienttype> in your jloadrConfig.xml .");
      }
    }
    return loaderConfig;
  }

  /**
   * Starts the actual client as a process with the prepared commands
   * @return The running client process
   * @throws IOException if the exitValue is not 0
   * @throws InterruptedException if the client process gets interrupted
   */
  private Process _startClientProcess() throws IOException, InterruptedException
  {
    Process process = new ProcessBuilder(commands)
          .directory(workingDirectory.toFile())
          .inheritIO()
          .start();

    process.waitFor(IOption.WAIT_FOR_START,TimeUnit.SECONDS);

    if(!process.isAlive() && process.exitValue()!=0)
      throw new RuntimeException("application exited with exit code "+ process.exitValue());

    return process;
  }
}

