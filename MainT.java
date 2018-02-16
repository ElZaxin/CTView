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
        }catch(Exception e) {}
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
          image1 = bilinearInterpolation(image1, image.getHeight() * 3, image.getWidth() * 3);
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
	double bilinearXAxis(double x, double y, double v1, double x1, double y1, double v2, double x2, double y2) {
		return v1 + (v2 - v1) * ((x - x1) / (x2 - x1));
	}
	double bilinearYAxis(double x, double y, double v1, double x1, double y1, double v2, double x2, double y2) {
		return v1 + (v2 - v1) * ((y - y1) / (y2 - y1));
	}
	byte[] intToByte(int i) {
		byte[] a = new byte[4];
		a[0] = (byte) (i >> 24);
		a[1] = (byte) (i >> 16);
		a[2] = (byte) (i >> 8);
		a[3] = (byte) i;
		return a;
		
	}
	int byteToInt(byte[] b) {
		return b[0] << 24 | (b[1] & 0xFF) << 16 | (b[2] & 0xFF) << 8 | (b[3] & 0xFF);
	}
	 public BufferedImage bilinearInterpolation(BufferedImage image, float newHeight, float newWidth) {
       	BufferedImage newImage = new BufferedImage((int) newWidth, (int) newHeight,BufferedImage.TYPE_3BYTE_BGR);
    	
		//set the origin points
       	float oldHeight = image.getHeight();
       	float oldWidth = image.getWidth();
		float yRatio = (newHeight/oldHeight);
		float xRatio = (newWidth/oldWidth);
		//pixels between each origin point = ratio
		
    	for(int j = 0; j < oldHeight - 1; j++) {
    		for(int i = 0; i < oldWidth - 1; i++) {
					
    				int y = (int) (j * yRatio);
    				int x = (int) (i * xRatio);
					
					int c1 = image.getRGB(i, j);
					//calculate other pixels to assign to thing
					for(int i = 0; i < (int) xRatio / 2; i++) {
						byte[] originalColour = intToByte(image.getRGB(i, j));
						byte[] point1Colour = intToByte(image.getRGB(i - 1, j));
						byte[] point2Colour = intToByte(image.getRGB(i + 1, j));
						
					}
					for(int i = 0; i < (int) yRatio / 2; i++) {
						
					}
					
					
					//assign origin pixel to new image
					newImage.setRGB(x, y, c1);
								
					
			}
    	}
		//loop through every origin pixel, 
		for(int j = 1; j < newImage.getHeight() - 1; j++) {
			for(int i = 1; i < newImage.getWidth() - 1; i++) {
				if(newImage.getRGB(i, j) == -16777216) {
					int scaleX = (int) xRatio / 2;
					byte[] originalColour = intToByte(newImage.getRGB(i, j));
					byte[] point1Colour = intToByte(newImage.getRGB(i - scaleX, j));
					byte[] point2Colour = intToByte(newImage.getRGB(i + scaleX, j));
					
					//red channel because java getrgb is stored in argb
					originalColour[1] = (byte) bilinearXAxis(i, j, point1Colour[1], i - scaleX, j, point2Colour[1], i + scaleX, j);
					
					//green channel
					originalColour[2] = (byte) bilinearXAxis(i, j, point1Colour[2], i - scaleX, j, point2Colour[2], i + scaleX, j);
					
					//blue channel
					originalColour[3] = (byte) bilinearXAxis(i, j, point1Colour[3], i - scaleX, j, point2Colour[3], i + scaleX, j);
					
					
					
					newImage.setRGB(i, j, byteToInt(originalColour));
					
				}
				/*
					//find uncoloured x coordinate
					for(int k = 1; k < xRatio; k++) {

						//find the colour along x axis where  
						//origin point, point to find
						
					}
					
					//find uncoloured y coordinate
					//find uncoloured x coordinate
					for(int l = 1; l < yRatio; l++) {

						//find the colour along x axis where  
						//origin point, point to find
						double v = 0; //colour to find
						double xf = x; //x value of colour to find
						double yf = y + l; //y because same y value
						
						//point to the left, ie, point 1
						double x1 = x;
						double y1 = y;
						double v1 = c1;
						
						//point to the right, ie, point 2
						double x2 = x;
						double y2 = y + yRatio;
						double v2 = image.getRGB(i, j + 1); //find the value of the original image where the colour will be
						
						v = bilinearYAxis(xf, yf, v1, x1, y1, v2, x2, y2);
						newImage.setRGB((int) xf, (int) yf, (int) v);
					}*/
			}
			
		}
		
		//find the points inbetween
		
		
    	return newImage;
    }

}
}
