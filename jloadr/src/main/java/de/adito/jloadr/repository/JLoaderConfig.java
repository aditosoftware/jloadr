package de.adito.jloadr.repository;

import de.adito.jloadr.api.IResourceId;
import de.adito.jloadr.common.*;
import org.w3c.dom.*;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;

/**
 * @author j.boesl, 22.12.16
 */
public class JLoaderConfig
{
  public static final IResourceId CONFIG_ID = new ResourceId("jloadrConfig.xml");

  private static final String TAG_JAVA = "javaHome";
  private static final String TAG_VM_OPTION = "vmOption";
  private static final String TAG_SYSTEM_PROPERTY = "systemProperty";
  private static final String TAG_CLASSPATH = "classpath";
  private static final String TAG_MAIN = "main";
  private static final String TAG_ARGUMENT = "argument";

  private static final String TAG_USEJAVACLIENT = "usejavaclient";
  private static final String TAG_EXECPATH = "execpath";
  private static final String TAG_DEFAULTSERVER = "defaultserver";

  private String javaHome;
  private List<String> vmParameters;
  private List<String> systemParameters;
  private List<String> classpath;
  private String mainCls;
  private List<String> arguments;

  private String useJavaClient;
  private String execPath;
  private String defaultServer;


  public void load(InputStream pInputStream)
  {
    Document document = XMLUtil.loadDocument(pInputStream);
    Element root = document.getDocumentElement();

    useJavaClient = XMLUtil.getChildText(root, TAG_USEJAVACLIENT);

    javaHome = XMLUtil.getChildText(root, TAG_JAVA);

    vmParameters = XMLUtil.findChildElements(root, TAG_VM_OPTION).stream()
          .map(element -> element.getTextContent().trim())
          .collect(Collectors.toList());

    systemParameters = XMLUtil.findChildElements(root, TAG_SYSTEM_PROPERTY).stream()
          .map(element -> element.getTextContent().trim())
          .collect(Collectors.toList());

    classpath = XMLUtil.findChildElements(root, TAG_CLASSPATH).stream()
          .map(element -> element.getTextContent().trim())
          .collect(Collectors.toList());

    mainCls = XMLUtil.getChildText(root, TAG_MAIN);
    assert mainCls != null;

    arguments = XMLUtil.findChildElements(root, TAG_ARGUMENT).stream()
          .map(element -> element.getTextContent().trim())
          .collect(Collectors.toList());


    execPath = XMLUtil.getChildText(root, TAG_EXECPATH);
    defaultServer = XMLUtil.getChildText(root, TAG_DEFAULTSERVER);
  }

  public void save(OutputStream pOutputStream)
  {
    XMLUtil.saveDocument(pOutputStream, pDocument -> {
      Element root = pDocument.createElement("jloadr");
      pDocument.appendChild(root);
      _append(pDocument, root, TAG_USEJAVACLIENT, useJavaClient);
      _append(pDocument, root, TAG_JAVA, javaHome);
      _append(pDocument, root, TAG_VM_OPTION, vmParameters);
      _append(pDocument, root, TAG_SYSTEM_PROPERTY, systemParameters);
      _append(pDocument, root, TAG_CLASSPATH, classpath);
      _append(pDocument, root, TAG_MAIN, mainCls);
      _append(pDocument, root, TAG_ARGUMENT, arguments);
      _append(pDocument, root, TAG_EXECPATH, execPath);
      _append(pDocument, root, TAG_DEFAULTSERVER, defaultServer);
    });
  }

  public String[] getExecStartCommands(Path pWorkingDirectory)
  {
    String execFile = String.valueOf(pWorkingDirectory) + File.separatorChar + getExecPath();
    //try java client if not available
    if(execFile == null || execFile.isEmpty())
      getStartCommands(pWorkingDirectory, JLoadrUtil.getAdditionalSystemParameters());
    List <String> parameters = new ArrayList<>();
    parameters.add(execFile);
    parameters.add(getDefaultServer());

    return parameters.toArray(new String[parameters.size()]);
  }

  public String[] getStartCommands(Path pWorkingDirectory, List<String> pAdditionalSystemParameters)
  {
    String mainCls = getMainCls();
    if (mainCls == null || mainCls.isEmpty())
      throw new RuntimeException("Application can't be started. No main class provided.");

    List<String> parameters = new ArrayList<>();
    parameters.add(_getStartJavaCommand(pWorkingDirectory));

    getVmParameters().stream()
        .map(param -> param.startsWith("-") ? param : "-" + param)
        .forEach(parameters::add);

    Stream.concat(getSystemParameters().stream(), pAdditionalSystemParameters == null ? Stream.empty() : pAdditionalSystemParameters.stream())
        .map(param -> param.startsWith("-D") ? param : "-D" + param)
        .forEach(parameters::add);

    String cp = getClasspath().stream()
        .map(str -> str.replace('/', File.separatorChar))
        .collect(Collectors.joining(File.pathSeparator));
    if (!cp.isEmpty())
    {
      parameters.add("-cp");
      parameters.add(cp);
    }
    parameters.add(mainCls);
    parameters.addAll(getArguments());

    return parameters.toArray(new String[parameters.size()]);
  }

  private String _getStartJavaCommand(Path pWorkingDirectory)
  {
    String javaHome = getJavaHome();
    return ProcessUtil.findJavaCmd(pWorkingDirectory, javaHome);
  }

  public String getJavaHome()
  {
    return javaHome;
  }
  public void setJavaHome(String pJavaHome)
  {
    javaHome = pJavaHome;
  }

  public List<String> getVmParameters()
  {
    return vmParameters == null ? Collections.emptyList() : vmParameters;
  }
  public void setVmParameters(List<String> pVmParameters)
  {
    vmParameters = pVmParameters;
  }

  public List<String> getSystemParameters()
  {
    return systemParameters == null ? Collections.emptyList() : systemParameters;
  }
  public void setSystemParameters(List<String> pSystemParameters)
  {
    systemParameters = pSystemParameters;
  }

  public List<String> getClasspath()
  {
    return classpath;
  }
  public void setClasspath(List<String> pClasspath)
  {
    classpath = pClasspath;
  }

  public String getMainCls()
  {
    return mainCls;
  }
  public void setMainCls(String pMainCls)
  {
    mainCls = pMainCls;
  }

  public List<String> getArguments()
  {
    return arguments;
  }
  public void setArguments(List<String> pArguments)
  {
    arguments = pArguments;
  }

  public String getUseJavaClient()
  {
    return useJavaClient;
  }
  public void setUseJavaClient(String pUseJavaClient)
  {
    useJavaClient = pUseJavaClient;
  }

  public String getExecPath()
  {
    return execPath;
  }
  public void setExecPath(String pExecPath)
  {
    execPath = pExecPath;
  }

  public String getDefaultServer()
  {
    return defaultServer;
  }
  public void setDefaultServer(String pDefaultServer)
  {
    defaultServer = pDefaultServer;
  }

  private void _append(Document pDocument, Element pAppendTo, String pTag, String pValue)
  {
    if (pValue != null)
    {
      Element element = pDocument.createElement(pTag);
      element.setTextContent(pValue);
      pAppendTo.appendChild(element);
    }
  }

  private void _append(Document pDocument, Element pAppendTo, String pTag, List<String> pValues)
  {
    if (pValues != null)
    {
      for (String value : pValues)
      {
        Element element = pDocument.createElement(pTag);
        element.setTextContent(value);
        pAppendTo.appendChild(element);
      }
    }
  }

}
