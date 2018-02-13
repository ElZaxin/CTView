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


public class Main extends JFrame {
	
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
		  
	       Main e = new Main();
	       e.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	       e.start();
	    }

private class GUIEventHandler implements ActionListener {

    public void actionPerformed(ActionEvent event) {
          image1 = bilinearInterpolation(image1, image.getHeight() * 2, image.getWidth() * 2);
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
	 public BufferedImage bilinearInterpolation(BufferedImage image, float newHeight, float newWidth) {
       	BufferedImage newImage = new BufferedImage((int) newWidth, (int) newHeight,BufferedImage.TYPE_3BYTE_BGR);
    	
		//set the origin points
       	float oldHeight = image.getHeight();
       	float oldWidth = image.getWidth();
		float yRatio = (newHeight/oldHeight);
		float xRatio = (newWidth/oldWidth);
		//pixels between each origin point = ratio
		
    	for(int j = 0; j < oldHeight; j++) {
    		for(int i = 0; i < oldWidth; i++) {
					
    				int y = (int) (j * yRatio);
    				int x = (int) (i * xRatio);
					
					int c1 = image.getRGB(i, j);
					
					
    				newImage.setRGB(x, y, c1);
					
					//find uncoloured x coordinate
					for(int k = 1; k < xRatio; k++) {
						//find the gradient 
						
						newImage.setRGB(x + k, y, image.getRGB(i, j));
					}
					
					//find uncoloured y coordinate
					
					
					
			}
    	}
		
		//find the points inbetween
		
		
    	return newImage;
    }

}
}
