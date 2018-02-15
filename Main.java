import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.event.*;

// OK this is not best practice - maybe you'd like to create
// a volume data class?
// I won't give extra marks for that though.

public class Main extends JFrame {
	final static int X_MAX = 256; //constants for the maximum x y and z of the data
	final static int Y_MAX = 256;
	final static int Z_MAX = 113;
	String state = "FV"; //the state, either side view front view top	view or rotating
	//TV - top view
	//SV - side view
	//FV - front view
	
    JButton mip_button; //an example button to switch to MIP mode
    JLabel image_icon1; //using JLabel to display an image (check online documentation)
    JLabel image_icon2; //using JLabel to display an image (check online documentation)
	JSlider zslice_slider, yslice_slider; //sliders to step through the slices (z and y directions) (remember 113 slices in z direction 0-112)
    JSlider xslice_slider;
	BufferedImage image1, image2; //storing the image in memory
	short cthead[][][]; //store the 3D volume data set
	short min, max; //min/max value in the 3D volume data set
	
    /*
        This function sets up the GUI and reads the data set
    */
    public void start() throws IOException {
        //File name is hardcoded here - much nicer to have a dialog to select it and capture the size from the user
		File file = new File("CThead");
        
        //Create a BufferedImage to store the image data
		image1=new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR);
		image2=new BufferedImage(256, 112, BufferedImage.TYPE_3BYTE_BGR);

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
        
        // Then our image (as a label icon)
        image_icon1=new JLabel(new ImageIcon(image1));
        container.add(image_icon1);
		
		image_icon2 = new JLabel(new ImageIcon(image2));
        container.add(image_icon2);
 
        // Then the invert button
        mip_button = new JButton("MIP");
        container.add(mip_button);
		
        
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
		
		//see
		//https://docs.oracle.com/javase/7/docs/api/javax/swing/JSlider.html
		//for documentation (e.g. how to get the value, how to display vertically if you want)
        
        // Now all the handlers class
        GUIEventHandler handler = new GUIEventHandler();

        // associate appropriate handlers
        mip_button.addActionListener(handler);
		yslice_slider.addChangeListener(handler);
        zslice_slider.addChangeListener(handler);
        xslice_slider.addChangeListener(handler);
        
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
		}
		
		//action handlers (e.g. for buttons)
         public void actionPerformed(ActionEvent event) {
                 if (event.getSource()==mip_button) {
                        image2=MIP(image2); 
						
                        image_icon2.setIcon(new ImageIcon(image2));
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
    	//float col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
    	for(int c = 0; c < 3; c++) {
    		int index = c + (3 * i) + (3 * j * width);
			data[index] = (byte) datum;
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
    	return newImage;
    	
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
    	
    	return resizeNearestNeighbour(newImage, 256, 256);
    	
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
    	return resizeNearestNeighbour(newImage, 256, 256);
    }
    //*********************************************
    //Image manipulation, resizing and maximum intensity projection
    //functions
	//resizing functions
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
    
	//bilinear interpolation resizing
	public BufferedImage resizeInterPolation(BufferedImage image, float newHeight, float newWidth) {
		BufferedImage newImage = new BufferedImage((int) newWidth, (int) newHeight, BufferedImage.TYPE_3BYTE_BGR);
		
		float oldHeight = image.getHeight();
		float oldWidth = image.getWidth();
		for(int j = 0; j < newHeight; j++) {
			for(int i = 0; i < newWidth; i++) {
				//new rgb 
				float r;
				float g;
				float b;
				
				//x and y values from previous image
				float y;
				float x;
				
			}
		}
		return null;
		
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
       
        //Obtain pointer to data for fast processing
        byte[] data = GetImageData(newImage);

				for (i = 0; i < X_MAX; i++) {
					for (j=0; j < Y_MAX; j++) {
						maximum = 0;
						for(k=0; k < Z_MAX; k++) {//loop through every z value, and find the highest intensitity
							short temp = cthead[k][i][j];
							if(temp > maximum) {
							maximum = temp;
						}
					}
					datum = maximum;
                	assignColour(data, datum, i, j, width);
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
		
		
        

        return resizeNearestNeighbour(newImage, 256, 256);
}	

	/**
	* histogram equalisation
	*/
	public void histogramEqualisationData2() {
		/*(int histogramSize = max - min + 1;
		histogram = new int[histogramSize];
		System.out.println(min);
		//create the histogram, every value of cthead
		for(int z = 0; z < 113; z++) {
			for(int y = 0; y < 256; y++) {
				for(int x = 0; x < 256; x++) {
					int index = cthead[z][y][x] - min;
					histogram[index]++;
					
				}
			}
		}
		for(int i = 0; i < histogramSize; i++) {
			t[i] = t[i] + histogram[i];
			mapping[i] = 255.0f * (t[i] / histogramSize);
		}*/
	}
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
		
		
	}

    public static void main(String[] args) throws IOException {
 
       Main e = new Main();
       e.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       e.start();
    }
}