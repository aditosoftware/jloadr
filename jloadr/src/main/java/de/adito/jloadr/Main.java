package de.adito.jloadr;

import de.adito.jloadr.clientstarter.*;
import de.adito.jloadr.common.*;
import org.w3c.dom.*;

import java.io.*;
import java.nio.file.*;

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

    ClientStarter clientStarter = new ClientStarter(url, iconPath, startName);
    clientStarter.start();
  }
}