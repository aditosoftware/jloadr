package de.adito.jloadr.common;

import org.w3c.dom.*;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author j.boesl, 22.12.16
 */
public class JLoaderConfig
{
  public static final String JAVA = "java";
  public static final String VM_PARAMETER = "vmParameter";
  public static final String CLASSPATH = "classpath";
  public static final String MAIN = "main";
  public static final String ARGUMENT = "argument";

  private String javaCmd;
  private List<String> vmParameters;
  private List<String> classpath;
  private String mainCls;
  private List<String> arguments;


  public void load(InputStream pInputStream)
  {
    Document document = XMLUtil.loadDocument(pInputStream);
    Element root = XMLUtil.getChildElement(document.getDocumentElement(), "jloadr");

    javaCmd = XMLUtil.getChildText(root, JAVA);

    vmParameters = XMLUtil.findChildElements(root, VM_PARAMETER).stream()
        .map(element -> element.getTextContent().trim())
        .collect(Collectors.toList());

    classpath = XMLUtil.findChildElements(root, CLASSPATH).stream()
        .map(element -> element.getTextContent().trim())
        .collect(Collectors.toList());

    mainCls = XMLUtil.getChildText(root, MAIN);
    assert mainCls != null;

    arguments = XMLUtil.findChildElements(root, ARGUMENT).stream()
        .map(element -> element.getTextContent().trim())
        .collect(Collectors.toList());
  }

  public void save(OutputStream pOutputStream)
  {
    XMLUtil.saveDocument(pOutputStream, pDocument -> {
      Element root = pDocument.createElement("jloadr");
      pDocument.getDocumentElement().appendChild(root);
      _append(pDocument, root, JAVA, javaCmd);
      _append(pDocument, root, VM_PARAMETER, vmParameters);
      _append(pDocument, root, CLASSPATH, classpath);
      _append(pDocument, root, MAIN, mainCls);
      _append(pDocument, root, ARGUMENT, arguments);
    });
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
