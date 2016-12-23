package de.adito.jloadr;

import de.adito.jloadr.common.XMLUtil;
import org.w3c.dom.*;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 22.12.16
 */
public class JLoaderConfig
{
  public static final String CONFIG_NAME = "jloadrConfig.xml";

  public static final String TAG_JAVA = "java";
  public static final String TAG_VM_PARAMETER = "vmParameter";
  public static final String TAG_CLASSPATH = "classpath";
  public static final String TAG_MAIN = "main";
  public static final String TAG_ARGUMENT = "argument";

  private String javaCmd;
  private List<String> vmParameters;
  private List<String> classpath;
  private String mainCls;
  private List<String> arguments;


  public void load(InputStream pInputStream)
  {
    Document document = XMLUtil.loadDocument(pInputStream);
    Element root = document.getDocumentElement();

    javaCmd = XMLUtil.getChildText(root, TAG_JAVA);

    vmParameters = XMLUtil.findChildElements(root, TAG_VM_PARAMETER).stream()
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
  }

  public void save(OutputStream pOutputStream)
  {
    XMLUtil.saveDocument(pOutputStream, pDocument -> {
      Element root = pDocument.createElement("jloadr");
      pDocument.appendChild(root);
      _append(pDocument, root, TAG_JAVA, javaCmd);
      _append(pDocument, root, TAG_VM_PARAMETER, vmParameters);
      _append(pDocument, root, TAG_CLASSPATH, classpath);
      _append(pDocument, root, TAG_MAIN, mainCls);
      _append(pDocument, root, TAG_ARGUMENT, arguments);
    });
  }

  public String[] getStartCommands()
  {
    List<String> parameters = new ArrayList<>();
    parameters.add(getJavaCmd().replaceAll("/", File.separator));
    String vmParams = getVmParameters().stream()
        .map(param -> "-D" + param)
        .collect(Collectors.joining(" "));
    if (!vmParams.isEmpty()) {
      parameters.add(vmParams);
    }
    String cp = getClasspath().stream()
        .map(str -> str.replaceAll("/", File.separator))
        .collect(Collectors.joining(File.pathSeparator));
    if (!cp.isEmpty()) {
      parameters.add("-cp");
      parameters.add(cp);
    }
    parameters.add(getMainCls());
    getArguments().forEach(parameters::add);

    return parameters.toArray(new String[parameters.size()]);
  }

  public String getJavaCmd()
  {
    return javaCmd == null ? "java" : javaCmd;
  }

  public void setJavaCmd(String pJavaCmd)
  {
    javaCmd = pJavaCmd;
  }

  public List<String> getVmParameters()
  {
    return vmParameters;
  }

  public void setVmParameters(List<String> pVmParameters)
  {
    vmParameters = pVmParameters;
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

  private void _append(Document pDocument, Element pAppendTo, String pTag, String pValue)
  {
    if (pValue != null) {
      Element element = pDocument.createElement(pTag);
      element.setTextContent(pValue);
      pAppendTo.appendChild(element);
    }
  }

  private void _append(Document pDocument, Element pAppendTo, String pTag, List<String> pValues)
  {
    if (pValues != null) {
      for (String value : pValues) {
        Element element = pDocument.createElement(pTag);
        element.setTextContent(value);
        pAppendTo.appendChild(element);
      }
    }
  }

}
