package emit.ipcv.utils;

import emit.ipcv.utils.colors.RGBA;
import java.awt.Color;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class ImageLoader implements Serializable {

  private BufferedImage bufferedImage;
  private int[][] grayscale;
  private RGBA[][] originalColor;
  private String imageOriginalExtension;

  public ImageLoader(File localisation) throws IOException {
    this.bufferedImage = ImageIO.read(localisation);
  }

  public ImageLoader(String localisation) throws IOException {
    this.bufferedImage = ImageIO.read(new File(localisation));
  }

  public ImageLoader(BufferedImage bufferedImage) {
    this.bufferedImage = bufferedImage;
  }

  public RGBA[][] getOriginalColor() {
    if(originalColor != null) return originalColor;
    final int ligne = bufferedImage.getWidth(), colonne = bufferedImage.getHeight();
    originalColor = new RGBA[ligne][colonne];
    for (int x = 0; x < ligne; x++) {
      for (int y = 0; y < colonne; y++) {
        Color color = new Color(bufferedImage.getRGB(x, y), true);
        originalColor[x][y] = new RGBA(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
      }
    }
    return originalColor;
  }

  private int[][] getLightningGrayscale() {
    final int ligne = bufferedImage.getWidth(), colonne = bufferedImage.getHeight();
    RGBA[][] originale = this.getOriginalColor();
    this.grayscale = new int[ligne][colonne];
    for (int x = 0; x < ligne; x++) {
      for (int y = 0; y < colonne; y++) {
        this.grayscale[x][y] = ((originale[x][y].max() + originale[x][y].min()) / 2);
      }
    }
    return this.grayscale;
  }

  private int[][] getAverageGayscale() {
    final int ligne = bufferedImage.getWidth(), colonne = bufferedImage.getHeight();
    RGBA[][] originale = this.getOriginalColor();
    this.grayscale = new int[ligne][colonne];
    for (int x = 0; x < ligne; x++) {
      for (int y = 0; y < colonne; y++) {
        this.grayscale[x][y] = (int) ((float) (originale[x][y].getRed() + originale[x][y].getGreen() + originale[x][y].getBlue()) / (float) 3);
      }
    }
    return this.grayscale;
  }

  private int[][] getLuninanceGrayscale() {
    final int ligne = bufferedImage.getWidth(), colonne = bufferedImage.getHeight();
    RGBA[][] originale = this.getOriginalColor();
    this.grayscale = new int[ligne][colonne];
    for (int x = 0; x < ligne; x++) {
      for (int y = 0; y < colonne; y++) {
        this.grayscale[x][y] = (int) (0.21 * originale[x][y].getRed() + 0.72 * originale[x][y].getGreen() + 0.07 * originale[x][y].getBlue());
      }
    }
    return this.grayscale;
  }

  public BufferedImage getBufferedImage() throws IOException {
    return bufferedImage;
  }

  public int[][] getGrayscale() {
    return this.getAverageGayscale();
  }

  public void setOriginalColor(RGBA[][] image) {
    this.originalColor = image;
  }

  public void setGrayScale(int[][] grayscale) {
    this.grayscale = grayscale;
  }
  
  public String getImageOriginalExtension() {
    return imageOriginalExtension;
  }
  
  public void setImageOriginalExtension(String imageOriginalExtension) {
    this.imageOriginalExtension = imageOriginalExtension.toLowerCase();
  }
}
