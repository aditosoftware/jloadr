package de.adito.jloadr.bootstrap;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ShowErrorUtil {

  public ShowErrorUtil(Throwable loadError, Throwable pE) throws IOException {
    String exceptionMessage = "";
    String smallExceptionMessage = pE.getMessage();

    if (loadError != null) {
      smallExceptionMessage = loadError.getMessage();
      exceptionMessage += BootstrapUtil.stackTraceToString(loadError) + "\n\n";
    }
    exceptionMessage += BootstrapUtil.stackTraceToString(pE);

    showError(smallExceptionMessage, exceptionMessage);
  }


  protected void showError(String pSmallMessage, String pMessage) {
    String title = UIManager.getString("OptionPane.messageDialogTitle");
    JTextArea textArea = new JTextArea("A problem occurred.\n " + pSmallMessage);
    textArea.setEditable(false);
    textArea.setOpaque(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);

    String[] options = new String[]{
        "Cancel",
        "Details"
    };
    int r = JOptionPane.showOptionDialog(
        null, textArea, title,
        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
        null, options, options[0]);

    if (r == 1)
      _extendedDialog(title, pMessage);

  }

  protected void _extendedDialog(String pTitle, String pMessage) {
    JTextArea textArea = new JTextArea(pMessage);
    textArea.setEditable(false);
    textArea.setOpaque(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    JScrollPane scrollPane = new JScrollPane(textArea);
    scrollPane.setBorder(null);
    scrollPane.setPreferredSize(new Dimension(640, 400));

    JOptionPane.showOptionDialog(null, scrollPane, pTitle,
        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
        null, new String[]{"Cancel"}, null);
  }
}