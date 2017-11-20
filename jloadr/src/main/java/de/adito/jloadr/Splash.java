package de.adito.jloadr;

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
class Splash extends JFrame implements ILoader.IStateCallback
{

  private int elementCount;
  private int currentElement;
  private JLabel splashLabel;


  Splash(String pIconPath, String pStartName) throws HeadlessException
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
  }

  @Override
  public void setSplashResource(IResource pSplashResource)
  {
    SwingUtilities.invokeLater(() -> {
      if (splashLabel == null)
      {
        splashLabel = new JLabel();
        splashLabel.setHorizontalAlignment(SwingConstants.CENTER);
        getContentPane().add(splashLabel);
      }

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
    });

    _update();
  }

  @Override
  public void setElementCount(int pElementCount)
  {
    elementCount = pElementCount;
  }

  @Override
  public void loaded(int pElementNumber)
  {
    currentElement = pElementNumber;
    _update();
  }

  @Override
  public void finished()
  {
    currentElement = elementCount;
    _update();
  }

  private void _update()
  {
    SwingUtilities.invokeLater(() -> {
      pack();
      setLocationRelativeTo(null);
      setVisible(true);

      revalidate();
      repaint();
    });
  }

  @Override
  public void paint(Graphics g)
  {
    super.paint(g);

    double loaded = elementCount == 0 ? 0 : (double) currentElement / (double) elementCount;

    int w = getContentPane().getWidth();
    int h = getContentPane().getHeight();
    g.setColor(SystemColor.GRAY);
    g.fillRect(4, h - 8, (int) Math.round((w - 8) * loaded), 4);
  }
}
