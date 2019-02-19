package de.adito.jloadr.bootstrap;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;

public class ShowErrorUtil
{

  private String specialExcM;
  /**
   * The ExceptionMessages are prepared to be displayed in a JDialog.
   * smallExceptionMessage will show the class name of the Throwable and the specified message, if existent or a customized message
   * from _identifyException();
   * @param loadError catched Exception while executing bootstrap
   * @param pE catched Exception while executing jloadr
   */
  public ShowErrorUtil(Throwable loadError, Throwable pE) throws IOException
  {
    String exceptionMessage = "";
    String smallExceptionMessage = _identifyException(pE) ? specialExcM : pE.toString();

    if (loadError != null)
    {
      smallExceptionMessage = _identifyException(loadError) ? specialExcM : loadError.toString();
      exceptionMessage += BootstrapUtil.stackTraceToString(loadError) + "\n\n";
    }
    exceptionMessage += BootstrapUtil.stackTraceToString(pE);

    showError(smallExceptionMessage, exceptionMessage);
  }

  /**
   * Shows a dialog with a small error message as simple as possible
   */
  private void showError(String pSmallMessage, String pMessage)
  {
    String title = UIManager.getString("OptionPane.messageDialogTitle");
    JTextArea textArea = new JTextArea("Something went wrong:\n\n" + pSmallMessage);
    textArea.setEditable(false);
    textArea.setOpaque(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBorder(null);
    scrollPane.setPreferredSize(new Dimension(300, 70));

    String[] options = new String[]
        {
        "Cancel",
        "Details"
    };

    JDialog dialog = new JDialog();
    dialog.setAlwaysOnTop(true);

    int r = JOptionPane.showOptionDialog(
        dialog, scrollPane, title,
        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
        null, options, options[0]);

    dialog.dispose();
    if (r == 1)
      _extendedDialog(title, pMessage);
  }

  /**
   * Shows the full stackTrace of bootstrap and jloadr
   */
  private void _extendedDialog(String pTitle, String pMessage)
  {
    JTextArea textArea = new JTextArea(pMessage);
    textArea.setEditable(false);
    textArea.setOpaque(false);

    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBorder(null);
    scrollPane.setPreferredSize(new Dimension(640, 400));
    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    JOptionPane.showOptionDialog(null, scrollPane, pTitle,
        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
        null, new String[]{"Cancel"}, null);
  }

  /**
   * This method will try to identify the given Exception and make a more user friendly message for the dialog.
   */
  private boolean _identifyException(Throwable pThrowable)
  {
    if(pThrowable instanceof FileNotFoundException)
    {
      specialExcM = "Could not find file" + _getExcMessage(pThrowable);
      return true;
    }
    if(pThrowable instanceof MalformedURLException)
    {
      specialExcM = "The given URL is malformed" + _getExcMessage(pThrowable);
      return true;
    }
    if(pThrowable instanceof ConnectException)
    {
      specialExcM = "An error occurred while attempting to connect" + _getExcMessage(pThrowable);
      return true;
    }
    if(pThrowable instanceof RuntimeException)
    {
      specialExcM = "A problem occurred during runtime" + _getExcMessage(pThrowable);
      return true;
    }
    return false;
  }

  /**
   * The LocalizedMessage of the Throwable will be added if existent.
   * @return the LocalizedMessage as String or ".\n"
   */
  private String _getExcMessage(Throwable pThrowable)
  {
    return pThrowable.getLocalizedMessage() != null ? "\n" + pThrowable.getLocalizedMessage() : ".\n";
  }
}