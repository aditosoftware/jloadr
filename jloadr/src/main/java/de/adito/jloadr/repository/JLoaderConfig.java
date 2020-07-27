package de.adito.jloadr.repository;

import de.adito.jloadr.api.IResourceId;
import de.adito.jloadr.common.*;
import org.w3c.dom.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * A Class for creating and handling the jloadrConfig.xml. As well as preparing the starting commands for the used client.
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

  private static final String TAG_CLIENT_TYPE = "clienttype";
  private static final String TAG_EXECNAME = "execname";
  private static final String TAG_EXEC_COMMANDS = "execcommands";

  private String javaHome;
  private List<String> vmParameters;
  private List<String> systemParameters;
  private List<String> classpath;
  private String mainCls;
  private List<String> arguments;

  private String clientType;
  private String execName;
  private String execCommands;


  public void loadConfigTags(InputStream pInputStream)
  {
    Document document = XMLUtil.loadDocument(pInputStream);
    Element root = document.getDocumentElement();

    clientType = XMLUtil.getChildText(root, TAG_CLIENT_TYPE);
    if (clientType == null)
      clientType = "java";

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


    execName = XMLUtil.getChildText(root, TAG_EXECNAME);
    execCommands = XMLUtil.getChildText(root, TAG_EXEC_COMMANDS);
  }

  /**
   * Saves all tags with their content in a xml document
   * @param pOutputStream
   */
  public void saveTagsAsXml(OutputStream pOutputStream)
  {
    XMLUtil.saveDocument(pOutputStream, pDocument -> {
      Element root = pDocument.createElement("jloadr");
      pDocument.appendChild(root);
      _append(pDocument, root, TAG_CLIENT_TYPE, clientType);
      _append(pDocument, root, TAG_JAVA, javaHome);
      _append(pDocument, root, TAG_VM_OPTION, vmParameters);
      _append(pDocument, root, TAG_SYSTEM_PROPERTY, systemParameters);
      _append(pDocument, root, TAG_CLASSPATH, classpath);
      _append(pDocument, root, TAG_MAIN, mainCls);
      _append(pDocument, root, TAG_ARGUMENT, arguments);
      _append(pDocument, root, TAG_EXECNAME, execName);
      _append(pDocument, root, TAG_EXEC_COMMANDS, execCommands);
    });
  }

  /**
   * Creates start parameters for starting an electron application
   * @param pWorkingDirectory
   * @return Start parameters for ProcessBuilder
   */
  public String[] getElectronStartCommands(Path pWorkingDirectory)
  {
    if(execName == null || execName.isEmpty())
      throw new RuntimeException("An executable name must be specified. Please check your jloadrConfig.xml");

    String execFilePath = _createElectronPath(pWorkingDirectory);

    List <String> parameters = new ArrayList<>();
    parameters.add(execFilePath);

    if(execCommands == null)
      execCommands = "";
    parameters.add(getExecCommands());

    return parameters.toArray(new String[parameters.size()]);
  }

  /**
   * Chooses the correct electron directory for the current operating system
   * @param pWorkingDirectory
   * @return path of the executable file
   */
  private String _createElectronPath(Path pWorkingDirectory)
  {
    String os = System.getProperty("os.name").toLowerCase();

    File workingDir = new File(String.valueOf(pWorkingDirectory));
    File[] fileList = workingDir.listFiles();
    if(fileList.length == 0)
      throw new RuntimeException("No client directory found in " + pWorkingDirectory + ". Please contact your administrator.");

    for (File directory: fileList)
    {
      if(directory.isDirectory() && directory.getName().contains(getExecName()))
      {
        if(os.contains("win") && directory.getName().contains("win") ||
            os.contains("linux") && directory.getName().contains("linux"))
          return directory + File.separator + getExecName();
      }
    }

    return null;
  }

  /**
   * Creates start parameters for starting a java application
   * @param pWorkingDirectory
   * @param pAdditionalSystemParameters
   * @return Start parameters for ProcessBuilder
   */
  public String[] getStartCommands(Path pWorkingDirectory, List<String> pAdditionalSystemParameters)
  {
    String mainCls = getMainCls();
    if (mainCls == null || mainCls.isEmpty())
      throw new RuntimeException("Application can't be started. No main class provided. Please add a correct main class to your startup config.");

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

  /**
   * Adds a Tag with its value to the document
   * @param pDocument
   * @param pAppendTo
   * @param pTag
   * @param pValue
   */
  private void _append(Document pDocument, Element pAppendTo, String pTag, String pValue)
  {
    if (pValue != null)
    {
      Element element = pDocument.createElement(pTag);
      element.setTextContent(pValue);
      pAppendTo.appendChild(element);
    }
  }

  /**
   * Adds a Tag with its values to the document
   * @param pDocument
   * @param pAppendTo
   * @param pTag
   * @param pValues
   */
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

  /**
   * Prepares the command for starting jars by fetching the jre
   * @param pWorkingDirectory
   */
  private String _getStartJavaCommand(Path pWorkingDirectory)
  {
    String javaHome = getJavaHome();
    return ProcessUtil.findJavaCmd(pWorkingDirectory, javaHome);
  }

  //getter and setter for all tags

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

  public String getClientType()
  {
    return clientType;
  }
  public void setClientType(String pClientType)
  {
    clientType = pClientType;
  }

  public String getExecName()
  {
    return execName;
  }
  public void setExecName(String pExecName)
  {
    execName = pExecName;
  }

  public String getExecCommands()
  {
    return execCommands;
  }
  public void setExecCommands(String pExecCommands)
  {
    execCommands = pExecCommands;
  }

}
