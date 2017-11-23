package de.adito.jloadr.gui;

import de.adito.jloadr.api.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;

/**
 * @author j.boesl, 17.11.17
 */
public class Splash extends JFrame implements ILoader.IStateCallback
{
  private int absoluteProgress;
  private double relativeProgress;

  private JLabel splashLabel;
  private JLabel mbLoadedLabel;


  public Splash(String pIconPath, String pStartName) throws HeadlessException
  {
    if (pIconPath != null)
    {
      Path path = Paths.get(pIconPath);
      if (Files.exists(path))
        setIconImage(new ImageIcon(path.toString()).getImage());
    }
    if (pStartName != null)
      setTitle(pStartName);

    setUndecorated(true);

    splashLabel = new JLabel();
    splashLabel.setHorizontalAlignment(SwingConstants.CENTER);
    getContentPane().add(splashLabel);

    mbLoadedLabel = new JLabel();
    mbLoadedLabel.setForeground(Color.GRAY);
    splashLabel.add(mbLoadedLabel);
  }

  @Override
  public void setSplashResource(IResource pSplashResource)
  {
    SwingUtilities.invokeLater(() -> {
      if (pSplashResource == null)
      {
        splashLabel.setIcon(null);
        splashLabel.setText("loading ...");
      }
      if (pSplashResource != null)
      {
        try
        {
          BufferedImage image = ImageIO.read(pSplashResource.getInputStream());
          splashLabel.setIcon(new ImageIcon(image));
          splashLabel.setText("");
        }
        catch (IOException pE)
        {
          // no image
        }
      }

      pack();
      setLocationRelativeTo(null);
      if (!isVisible())
        setVisible(true);
    });

    _update();
  }

  @Override
  public void setProgress(int pAbsolute, double pRelative)
  {
    absoluteProgress = pAbsolute;
    relativeProgress = pRelative;
    _update();
  }

  @Override
  public void finished()
  {
    relativeProgress = 1;
    _update();
  }

  private void _update()
  {
    SwingUtilities.invokeLater(() -> {
      revalidate();
      repaint();
    });
  }

  @Override
  public void paint(Graphics g)
  {
    int w = getContentPane().getWidth();
    int h = getContentPane().getHeight();

    String text = absoluteProgress / 1024 + " Mb";
    mbLoadedLabel.setText(text);
    mbLoadedLabel.setSize(mbLoadedLabel.getPreferredSize());
    mbLoadedLabel.setLocation(w - 4 - mbLoadedLabel.getWidth(), getHeight() - 12 - mbLoadedLabel.getHeight());

    super.paint(g);

    g.setColor(SystemColor.GRAY);
    g.fillRect(4, h - 8, (int) Math.round((w - 8) * relativeProgress), 4);
  }

}
