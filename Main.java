import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

public class Main extends JFrame {
   final static int X_MAX = 256; //constants for the maximum x y and z of the data
   final static int Y_MAX = 256;
   final static int Z_MAX = 113;
   String state = "FV"; //the state, either side view front view top	view or rotating
	//TV - top view
	//SV - side view
	//FV - front view
   boolean histogramEqualised = false; //used to record for the colour setting whether histogram equalisation has been
   //applied
	
   JButton mip_button; //set the mips
   JButton he_button; //histogram equalisation
   JLabel image_icon1; //using JLabel to display an image (check online documentation)
   JLabel image_icon2; //using JLabel to display an image (check online documentation)
   JSlider zslice_slider, yslice_slider; //sliders to step through the slices (z and y directions) (remember 113 slices in z direction 0-112)
   JSlider xslice_slider, size_slider; //and to change the size
   BufferedImage image1, image2; //storing the image in memory
   short cthead[][][]; //store the 3D volume data set
   short min, max; //min/max value in the 3D volume data set
   float size = 100; //the size, in %, 100 is normal
	
    /*
        This function sets up the GUI and reads the data set
    */
   public void start() throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
      File file = new File("CThead");
        
        //Create a BufferedImage to store the image data
      image1=new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
      image2=new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
   
   	//Read the data quickly via a buffer (in C++ you can just do a single fread - I couldn't find the equivalent in Java)
      DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
   
      int i, j, k; //loop through the 3D data set
   	
      min=Short.MAX_VALUE; max=Short.MIN_VALUE; //set to extreme values
      short read; //value read in
      int b1, b2; //data is wrong Endian (check wikipedia) for Java so we need to swap the bytes around
   	
      cthead = new short[113][256][256]; //allocate the memory - note this is fixed for this data set
   	//loop through the data reading it in
      for (k=0; k<113; k++) {
         for (j=0; j<256; j++) {
            for (i=0; i<256; i++) {
            	//because the Endianess is wrong, it needs to be read byte at a time and swapped
               b1=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types (C++ is so much easier!)
               b2=((int)in.readByte()) & 0xff; //the 0xff is because Java does not have unsigned types (C++ is so much easier!)
               read=(short)((b2<<8) | b1); //and swizzle the bytes around
               if (read<min) min=read; //update the minimum
               if (read>max) max=read; //update the maximum
               cthead[k][j][i]=read; //put the short into memory (in C++ you can replace all this code with one fread)
            }
         }
      }
      System.out.println(min+" "+max); //diagnostic - for CThead this should be -1117, 2248
   	//(i.e. there are 3366 levels of grey (we are trying to display on 256 levels of grey)
   	//therefore histogram equalization would be a good thing
   
   
   
        // Set up the simple GUI
        // First the container:
      Container container = getContentPane();
      container.setLayout(new FlowLayout());
        
      
      	//setting up the change size function
      JLabel sizeLabel = new JLabel("Change Size");
      size_slider = new JSlider(0, 200);
      size_slider.setMajorTickSpacing(50);
      size_slider.setMinorTickSpacing(20);
      size_slider.setOrientation(SwingConstants.VERTICAL);
      size_slider.setPaintTicks(true);
      size_slider.setPaintLabels(true);
      container.add(sizeLabel);
      container.add(size_slider);
      
          // Then our image (as a label icon)
      image_icon1=new JLabel(new ImageIcon(image1));
      container.add(image_icon1);
   	
      image_icon2 = new JLabel(new ImageIcon(image2));
      container.add(image_icon2);
      
        // Then the mip and histogram equalisation function
      mip_button = new JButton("MIP");
      container.add(mip_button);
      
      he_button = new JButton("Histogram Equalisation");
      container.add(he_button);
   
        //label
      JLabel zLabel = new JLabel("Z Axis");
      container.add(zLabel);
   	//Zslice slider
      zslice_slider = new JSlider(0,112);
      container.add(zslice_slider);
      zslice_slider.setMajorTickSpacing(20);
      zslice_slider.setMinorTickSpacing(5);
      zslice_slider.setPaintTicks(true);
      zslice_slider.setPaintLabels(true);
      zslice_slider.setOrientation(SwingConstants.VERTICAL);
      
   	//label
      JLabel yLabel = new JLabel("Y Axis");
      container.add(yLabel);
      yslice_slider = new JSlider(0,255);
      container.add(yslice_slider);
   	//Add labels (y slider as example)
      yslice_slider.setMajorTickSpacing(50);
      yslice_slider.setMinorTickSpacing(10);
      yslice_slider.setPaintTicks(true);
      yslice_slider.setPaintLabels(true);
      yslice_slider.setOrientation(SwingConstants.VERTICAL);
      
   	//label
      JLabel xLabel = new JLabel("X Axis");
      container.add(xLabel);
   	//xslice slider
      xslice_slider = new JSlider(0, 255);
      container.add(xslice_slider);
      xslice_slider.setMajorTickSpacing(50);
      xslice_slider.setMinorTickSpacing(10);
      xslice_slider.setPaintTicks(true);
      xslice_slider.setPaintLabels(true);
      xslice_slider.setOrientation(SwingConstants.VERTICAL);
   	
   	//see
   	//https://docs.oracle.com/javase/7/docs/api/javax/swing/JSlider.html
   	//for documentation (e.g. how to get the value, how to display vertically if you want)
        
        // Now all the handlers class
      GUIEventHandler handler = new GUIEventHandler();
   
        // associate appropriate handlers
      mip_button.addActionListener(handler);
      he_button.addActionListener(handler);
      yslice_slider.addChangeListener(handler);
      zslice_slider.addChangeListener(handler);
      xslice_slider.addChangeListener(handler);
      size_slider.addChangeListener(handler);
        
        // ... and display everything
      pack();
      setLocationRelativeTo(null);
      setVisible(true);
   
   }
    
    /*
        This is the event handler for the application
    */
   private class GUIEventHandler implements ActionListener, ChangeListener {
   
   	//Change handler (e.g. for sliders)
      public void stateChanged(ChangeEvent e) {
         Object a = e.getSource();
        	//z axis, top down view
         if(a == zslice_slider) {
         	//update the state
            state = "TV";
         	//viewing it from top down
            image1= topView(image1, zslice_slider.getValue()); //(although mine is called MIP, it doesn't do MIP)
         	
            	// Update image
            image_icon1.setIcon(new ImageIcon(image1));
         }
        	
        	//y axis, front view
         if(a == yslice_slider) {
            state = "FV";
            image1 = frontView(image1, yslice_slider.getValue());
         	
         	//update image
            image_icon1.setIcon(new ImageIcon(image1));
         	
         }
        	
        	//x axis, side view
         if(a == xslice_slider) {
            state = "SV";
            image1 = sideView(image1, xslice_slider.getValue());
         	
         	//update image
            image_icon1.setIcon(new ImageIcon(image1));
         	
         }
         //size slider
         if(a == size_slider) {
            size = size_slider.getValue();
           }
       }
   	
   	//action handlers (e.g. for buttons)
      public void actionPerformed(ActionEvent event) {
         if (event.getSource()==mip_button) {
            image2=MIP(image2); 
         			
            image_icon2.setIcon(new ImageIcon(image2));
            
         }
         if(event.getSource() == he_button) {
            histogramEqualisationData();
         }
      }
   }

    /*
        This function will return a pointer to an array
        of bytes which represent the image data in memory.
        Using such a pointer allows fast access to the image
        data for processing (rather than getting/setting
        individual pixels)
    */
   public static byte[] GetImageData(BufferedImage image) {
      WritableRaster WR=image.getRaster();
      DataBuffer DB=WR.getDataBuffer();
      if (DB.getDataType() != DataBuffer.TYPE_BYTE)
         throw new IllegalStateException("That's not of type byte");
          
      return ((DataBufferByte) DB).getData();
   }
    /*
     * Gets the colour a pixel should be given the data array of bytes
     * of the image
     */
   public void assignColour(byte[] data, short datum, int i, int j, int width) {
    	//calculate the colour by performing a mapping from [min,max] -> [0,255]
      float col;
      if(histogramEqualised) {
         col = datum;
      }
      else {
         col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
      }
      
      
      for(int c = 0; c < 3; c++) {
         int index = c + (3 * i) + (3 * j * width);
         data[index] = (byte) col;
      }
   }
    
    //*************************************************
    //View Functions 
    /*
     * Top View Function - 
     * This function displays the image top down, using the z axis
     */
   public BufferedImage topView(BufferedImage image, int zslice) {
      BufferedImage newImage = new BufferedImage(X_MAX, Y_MAX, BufferedImage.TYPE_3BYTE_BGR);
      int width = newImage.getWidth();
      byte[] data = GetImageData(newImage);
      for(int j = 0; j < Y_MAX; j++) {
         for(int i = 0; i < X_MAX; i++) {
            short datum = cthead[zslice][i][j];
         	
            assignColour(data, datum, i, j, width);
         }
      }
      float newSize = (float) Math.floor(256 * (size / 100));
      return bilinearInterpolation(newImage, newSize, newSize);
    	
   }
    /*
     * Front View Function - 
     * using the y axis
     */
   public BufferedImage frontView(BufferedImage image, int yslice) {
      BufferedImage newImage = new BufferedImage(Y_MAX, Z_MAX, BufferedImage.TYPE_3BYTE_BGR);
      int width = newImage.getWidth();
      byte[] data = GetImageData(newImage);
      for(int k = 0; k < Z_MAX; k++) {
         for(int j = 0; j < Y_MAX; j++) {
            short datum = cthead[k][yslice][j];
            assignColour(data, datum, j, k, width);
         }
      }
    	float newSize = (float) Math.floor(256 * (size / 100));
      return bilinearInterpolation(newImage, newSize, newSize);
    	
   }
    /*
     * Side View Function - 
     * using the x axis
     */
   public BufferedImage sideView(BufferedImage image, int xslice) {
      BufferedImage newImage = new BufferedImage(X_MAX, Z_MAX, BufferedImage.TYPE_3BYTE_BGR);
      int width = newImage.getWidth();
      byte[] data = GetImageData(newImage);
      for(int i = 0; i < X_MAX; i++) {
         for(int k = 0; k < Z_MAX; k++) {
            short datum = cthead[k][i][xslice];
            assignColour(data, datum, i, k, width);
         }
      }
      float newSize = (float) Math.floor(256 * (size / 100));
      return bilinearInterpolation(newImage, newSize, newSize);
   }
    //*********************************************
    //Image manipulation, resizing and maximum intensity projection
    //functions
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

   int getIndex(int x, int y, int c, int width) {
	   return c + 3 * x + 3 * y * width;
   
   }
   public BufferedImage bilinearInterpolation(BufferedImage image, float newHeight, float newWidth) {
      BufferedImage newImage = new BufferedImage((int) newWidth, (int) newHeight,BufferedImage.TYPE_3BYTE_BGR);
      byte[] oldData = GetImageData(image);
      byte[] newData = GetImageData(newImage);
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
               
            //find the byte arrays of the original points on the o image, the cube found from the origin point x and y
            byte[] point1Colour = {oldData[getIndex(x, y, 0, image.getWidth())], oldData[getIndex(x, y, 1, image.getWidth())], 
            		oldData[getIndex(x, y, 2, image.getWidth())]};
            
            byte[] point2Colour = {oldData[getIndex(x + 1, y, 0, image.getWidth())], oldData[getIndex(x + 1, y, 1, image.getWidth())], 
            		oldData[getIndex(x + 1, y, 2, image.getWidth())]};
            
            byte[] point3Colour = {oldData[getIndex(x, y + 1, 0, image.getWidth())], oldData[getIndex(x, y + 1, 1, image.getWidth())], 
            		oldData[getIndex(x, y + 1, 2, image.getWidth())]};
            
            byte[] point4Colour = {oldData[getIndex(x + 1, y + 1, 0, image.getWidth())], oldData[getIndex(x + 1, y + 1, 1, image.getWidth())], 
            		oldData[getIndex(x + 1, y + 1, 2, image.getWidth())]};
            
               
               //for the square of the new image
              
               
            byte[] newColour = new byte[3];
                     //colours given after being converted to unsigned bytes, 0 - 256 for the interpolation equations because of how
                     //the interpolation equations are applied
                     //apply x interpolation p -> p2
            byte r1 = bilinearXAxis(i, y, point1Colour[0] & 0xFF, x, y, point2Colour[0] & 0xFF, x2, y);
            byte g1 = bilinearXAxis(i, y, point1Colour[1] & 0xFF, x, y,  point2Colour[1] & 0xFF, x2, y);
            byte b1 = bilinearXAxis(i, y, point1Colour[2] & 0xFF, x, y,  point2Colour[2] & 0xFF, x2, y);                        
                     
                     //then x interpolation p3 -> p4
            byte r2 = bilinearXAxis(i, y2, point3Colour[0] & 0xFF, x, y2, point4Colour[0] & 0xFF, x2, y2);
            byte g2 = bilinearXAxis(i, y2, point3Colour[1] & 0xFF, x, y2,  point4Colour[1] & 0xFF, x2, y2);
            byte b2 = bilinearXAxis(i, y2, point3Colour[2] & 0xFF, x, y2,  point4Colour[2] & 0xFF, x2, y2);                        
                     
                     
                     //final setting of the colours and applying point r1 -> r2 y axis interpolation
            newColour[0] = bilinearYAxis(i, j, r1, i, y, r2, i, y2);
            newColour[1] = bilinearYAxis(i, j, g1, i, y, g2, i, y2);
            newColour[2] = bilinearYAxis(i, j, b1, i, y, b2, i, y2);
                     
                     //convert the new colour to an integer
            newData[getIndex(i, j, 0, newImage.getWidth())] = newColour[0];
            newData[getIndex(i, j, 1, newImage.getWidth())] = newColour[1];
            newData[getIndex(i, j, 2, newImage.getWidth())] = newColour[2];
         }
            
           
      }
      return newImage;
   }
    
    /*
    This function shows how to carry out an operation on an image.
    It obtains the dimensions of the image, and then loops through
    the image carrying out the copying of a slice of data into the
	image.
*/
   public BufferedImage MIP(BufferedImage image) {
   	
      short datum, maximum;
   	//make the variables used in the for loops so they can be changed depending on what view
   	//the user is on
      int i,j,k;
      int width = image.getWidth();
      BufferedImage newImage = null;
      switch(state) {
      	//top view case
         case "TV": 
            newImage = new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
         //Get image dimensions, and declare loop variables
           width = newImage.getWidth();
         //Obtain pointer to data for fast processing
            byte[] data = GetImageData(newImage);
         
            for (i = 0; i < X_MAX; i++) {
               for (j = 0; j < Y_MAX; j++) {
                  maximum = 0;
                  for(k=0; k < Z_MAX; k++) {//loop through every z value, and find the highest intensitity
                     short temp = cthead[k][j][i];
                     if(temp > maximum) {
                        maximum = temp;
                     }
                  }
                  datum = maximum;
                  assignColour(data, datum, j, i, width);
               } // column loop
            } // row loop
            break;
      		
         case "FV":
            newImage = new BufferedImage(256, 113, BufferedImage.TYPE_3BYTE_BGR);
         //Get image dimensions, and declare loop variables
            width = newImage.getWidth();
         //Obtain pointer to data for fast processing
            data = GetImageData(newImage);
         
         //make the variables used in the for loops so they can be changed depending on what view
         //the user is on
         
            for (i = 0; i < Y_MAX; i++) {
               for (k=0; k < Z_MAX; k++) {
                  maximum = 0;
                  for(j=0; j < X_MAX; j++) {//loop through every z value, and find the highest intensitity
                     short temp = cthead[k][j][i];
                     if(temp > maximum) {
                        maximum = temp;
                     }
                  }
                  datum = maximum;
                  assignColour(data, datum, i, k, width);
               } // column loop
            } // row loop
            break;
      		
         case "SV":
            newImage = new BufferedImage(256, 113, BufferedImage.TYPE_3BYTE_BGR);
         //Get image dimensions, and declare loop variables
            width = newImage.getWidth();
         //Obtain pointer to data for fast processing
            data = GetImageData(newImage);
         
         //make the variables used in the for loops so they can be changed depending on what view
         //the user is on
         
            for (i = 0; i < X_MAX; i++) {
               for (k=0; k < Z_MAX; k++) {
                  maximum = 0;
                  for(j=0; j < Y_MAX; j++) {//loop through every z value, and find the highest intensitity
                     short temp = cthead[k][i][j];
                     if(temp > maximum) {
                        maximum = temp;
                     }
                  }
                  datum = maximum;
                  assignColour(data, datum, i, k, width);
               } // column loop
            } // row loop
            break;
      }
   	
   	
        
      float newSize = (float) Math.floor(256 * (size / 100));
      return bilinearInterpolation(newImage, newSize, newSize);
   }	

	/**
	* histogram equalisation
	*/
	
   public void histogramEqualisationData() {
      int[] histogram;
   	//size is max - min values, how many different values are in 
   	//cthead, for each value, the index it finds, it adds one to that data index
      double histogramSize = max - min + 1;
      histogram = new int[(int) histogramSize];
   	//create the histogram, every value of cthead
      for(int z = 0; z < 113; z++) {
         for(int y = 0; y < 256; y++) {
            for(int x = 0; x < 256; x++) {
               int index = cthead[z][y][x] - min;
               histogram[index]++;
            	
            }
         }
      }
   	
   	//create the cumulative distribution function t, ie, the cumulative distribution based
   	//on the histogram of dat 
      double[] t = new double[histogram.length];
      int totalSize = 7405568;
   	//sets first to the first element in the histogram to avoid negative array indexes
      t[0] = histogram[0];
      for(int i = 1; i < histogram.length; i++) {
         t[i] = t[i - 1] + histogram[i];
      }
   	
   	//create the mapping, map the elements of the data set to a new range
   	//map the data elements to the histogram curve * 255, the cumulative distribution
   	//curve / size, to make 0 to 1, over 255, to make it a colour value
      double[] mapping = new double[histogram.length];
      for(int i = 0; i < histogram.length; i++) {
         mapping[i] = 255 * (t[i] / totalSize);
      }
   	
      for(int z = 0; z < 113; z++) {
         for(int y = 0; y < 256; y++) {
            for(int x = 0; x < 256; x++) {
               short data = cthead[z][y][x];
               cthead[z][y][x] = (short) mapping[data - min];
            	
            	
            }
         }
      }
      histogramEqualised = true;
   	
   }

   public static void main(String[] args) throws IOException {
   
      Main e = new Main();
      e.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      e.start();
   }
}
