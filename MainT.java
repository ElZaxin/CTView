import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.awt.Color;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


public class MainT extends JFrame {
	
   private static final long serialVersionUID = 1L;
   JLabel image;
   BufferedImage image1;
   JButton resize;
   JSlider xslice_slider;
   JSlider yslice_slider;
   public void start() {
      Container container = getContentPane();
      container.setLayout(new FlowLayout());
        
      try {
         image1 = ImageIO.read(new File("image.jpg"));
      }
      catch(Exception e) {}
      image = new JLabel(new ImageIcon(image1));
      container.add(image);
      resize = new JButton("Resize");
        
      GUIEventHandler handler = new GUIEventHandler();
      resize.addActionListener(handler);
      container.add(resize);
        
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
   }
	
   public static void main(String[] args) throws IOException {
   	  
      MainT e = new MainT();
      e.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      e.start();
   }

   private class GUIEventHandler implements ActionListener {
   
      public void actionPerformed(ActionEvent event) {
         double height = image.getHeight() * 2;
         double width =  image.getWidth() * 2;
         image1 = bilinearInterpolation(image1, (float) height, (float) width);
         
         image.setIcon(new ImageIcon(image1));
      }
   //nearest neighbour resizing
      public BufferedImage resizeNearestNeighbour(BufferedImage image, float newHeight, float newWidth) {
         BufferedImage newImage = new BufferedImage((int) newWidth, (int) newHeight,BufferedImage.TYPE_3BYTE_BGR);
      
         float oldHeight = image.getHeight();
         float oldWidth = image.getWidth();
         for(int j = 0; j < newHeight; j++) {
            for(int i = 0; i < newWidth; i++) {
               float y = j * (oldHeight/newHeight);
               float x = i * (oldWidth/newWidth);
            	
               newImage.setRGB(i, j, image.getRGB((int) x, (int) y));
            	
            	
            }
         }
         return newImage;
      }
   
   /**
   * bilinear interpolation equations, on the y axis
   * V = V1 + (V2 - V1) * ((y - y1) / (y2 - y1))
   *
   *On the x axis
   * V = V1 + (V2 - V1) * ((x - x1) / (x2 - x1))
   * 
   * where 
   * V1 			V			V2
   * (x1, y1) 		(x,y)		(x2,y2)
   
   * ( -65282) * (1/2) 
   * -810380
   */
      byte bilinearXAxis(double x, double y, double v1, double x1, double y1, double v2, double x2, double y2) {
         double value =  (v1 + (v2 - v1) * ((x - x1) / (x2 - x1)));
         value -= 128;
         //because the v1 and v2 values are given in now unsigned bytes (from 0 - 256) and the calculation is done using them
         //must be converted from the range 0 - 256 to -127 to 127 so the - 128 is used
         byte ret = (byte) value;
         
         return ret;
      }
      byte bilinearYAxis(double x, double y, double v1, double x1, double y1, double v2, double x2, double y2) {
         double value = (v1 + (v2 - v1) * ((y - y1) / (y2 - y1)));
         value -= 128;
         byte ret = (byte) value; 
            
         return ret;
      }
      byte[] intToByte(int i) {
        //converts i to bytes, where an integer is 32 bits, by shifting them all to the left, which 
        //leaves only one byte for each calculation.
         byte[] a = new byte[4];
          
         
         a[0] = (byte) (i >> 24);
         a[1] = (byte) (i >> 16);
         a[2] = (byte) (i >> 8);
         a[3] = (byte) i;
         
        
         return a;
      
      }
      int byteToInt(byte[] b) {
        //convert the byte array to an integer through the use of bitwise shifts, and ors them all together
        //which essentially, if the other patterns are moved to the left, as in added more 0s, it will be a 0 ORed with
        //the other binary values, which will set them to the values that were 0s, 0xFF conversion to convert the unsigned 
        //btyes to signed bytes, because the last one is going to be signed
         return b[0] << 24 | (b[1] & 0xFF) << 16 | (b[2] & 0xFF) << 8 | (b[3] & 0xFF);
      }
      public BufferedImage bilinearInterpolation(BufferedImage image, float newHeight, float newWidth) {
         BufferedImage newImage = new BufferedImage((int) newWidth, (int) newHeight,BufferedImage.TYPE_3BYTE_BGR);
      
      //set the origin points
         float oldHeight = image.getHeight();
         float oldWidth = image.getWidth();
         float yRatio = (newHeight/oldHeight);
         float xRatio = (newWidth/oldWidth);
         //the indexs of the original image that have been found while cycling through the new image
         //used to get the 4 points for bilinear interpolation
         
      //pixels between each origin point = ratio
         boolean[][] pixelsSet = new boolean[newImage.getWidth()][newImage.getHeight()];
         for(int j = 0; j < newHeight; j++) {
            for(int i = 0; i < newWidth; i++) {
               //find out if the point is an origin point or not
               
               //original colour of origin point
               //calculate other pixels to assign to the correct ratio
               //using the 4 points, + 1, -1, +1 +1 and -1 -1 to make a square where every point is found using 
               //bilinear interpolation
               /*
               square is
               p1 p2
               p3 p4 
               where p is between one of these
               find the value of x + a and y + b, that point
               */
               //x and y finds the next origin point, by x / xRatio, floored, is going to be 
               //the last point found, that is part of the original image
               //the next point is going to be, on the original image, +1, the next index
               //the index of that point on the new image is going to be that multiplied by the x ratio 
               //+ the xratio because the next point will always be xRatio pixels away from the previous
               //origin x and y points
               
               int x = (int) Math.floor(i / xRatio);
               int y = (int) Math.floor(j / yRatio);
               //if x goes to the end of the image, then set it to 2 below, so that when it is incremented
               //to find the colour of the original image the index does not go out of bounds
                if(x >= image.getWidth() - 1) {
                  x = image.getWidth() - 2;
               }
               if(y >= image.getHeight() - 1) {
                  y = image.getHeight() - 2;
               }
               
               int x2 = (int) ((x * xRatio) + xRatio);
              
               int y2 = (int) ((y * yRatio) + yRatio);
               
               
               byte[] point1Colour = intToByte(image.getRGB(x, y));
               byte[] point2Colour = intToByte(image.getRGB(x + 1, y));
               byte[] point3Colour = intToByte(image.getRGB(x, y + 1));
               byte[] point4Colour = intToByte(image.getRGB(x + 1, y + 1));
               
               //for the square of the new image
              
               
               byte[] newColour = new byte[4];
                     //colours given after being converted to unsigned bytes, 0 - 256 for the interpolation equations because of how
                     //the interpolation equations are applied
                     //apply x interpolation p -> p2
               byte r1 = bilinearXAxis(i, y, point1Colour[1] & 0xFF, x, y, point2Colour[1] & 0xFF, x2, y);
               byte g1 = bilinearXAxis(i, y, point1Colour[2] & 0xFF, x, y,  point2Colour[2] & 0xFF, x2, y);
               byte b1 = bilinearXAxis(i, y, point1Colour[3] & 0xFF, x, y,  point2Colour[3] & 0xFF, x2, y);                        
                     
                     //then x interpolation p3 -> p4
               byte r2 = bilinearXAxis(i, y2, point3Colour[1] & 0xFF, x, y2, point4Colour[1] & 0xFF, x2, y2);
               byte g2 = bilinearXAxis(i, y2, point3Colour[2] & 0xFF, x, y2,  point4Colour[2] & 0xFF, x2, y2);
               byte b2 = bilinearXAxis(i, y2, point3Colour[3] & 0xFF, x, y2,  point4Colour[3] & 0xFF, x2, y2);                        
                     
                     
                     //final setting of the colours and applying point r1 -> r2 y axis interpolation
               newColour[1] = bilinearYAxis(i, j, r1, i, y, r2, i, y2);
               newColour[2] = bilinearYAxis(i, j, g1, i, y, g2, i, y2);
               newColour[3] = bilinearYAxis(i, j, b1, i, y, b2, i, y2);
                     
                     //convert the new colour to an integer
               newImage.setRGB(i, j, byteToInt(newColour));
               
            }
            
           
         }
         return newImage;
      }
   }
}