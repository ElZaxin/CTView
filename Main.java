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
        image_icon2=new JLabel(new ImageIcon(image2));
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
				//viewing it from top down
				image1= topView(image1, zslice_slider.getValue()); //(although mine is called MIP, it doesn't do MIP)
	        
            	// Update image
            	image_icon1.setIcon(new ImageIcon(image1));
            }
        	
        	//y axis, front view
        	if(a == yslice_slider) {
        		image1 = frontView(image1, yslice_slider.getValue());
        		
        		//update image
        		image_icon1.setIcon(new ImageIcon(image1));
        		
        	}
        	
        	//x axis, side view
        	if(a == xslice_slider) {
        		System.out.println("x");
        		image1 = sideView(image1, xslice_slider.getValue());
        		
        		//update image
        		image_icon1.setIcon(new ImageIcon(image1));
        		
        	}
		}
		
		//action handlers (e.g. for buttons)
         public void actionPerformed(ActionEvent event) {
                 if (event.getSource()==mip_button) {
						//e.g. do something to change the image here
                        //e.g. call MIP function
                        image1=MIP(image1); //(although mine is called MIP, it doesn't do MIP)
        
                        // Update image
                        image_icon1.setIcon(new ImageIcon(image1));
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
    	float col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
    	for(int c = 0; c < 3; c++) {
    		int index = c + 3 * i + 3 * j * width;
    		data[index] = (byte) col;
    	}
    }
    /*
     * Top View Function - 
     * This function displays the image top down, using the z axis
     */
    public BufferedImage topView(BufferedImage image, int zslice) {
    	int width = image.getWidth();
    	int height = image.getHeight();
    	byte[] data = GetImageData(image);
    	
    	for(int j = 0; j < height; j++) {
    		for(int i = 0; i < width; i++) {
    			short datum = cthead[zslice][j][i];
    			
				assignColour(data, datum, i, j, width);
    		}
    	}
    	return image;
    	
    }
    /*
     * Front View Function - 
     * using the y axis
     */
    public BufferedImage frontView(BufferedImage image, int yslice) {
    	int width = image.getWidth();
    	byte[] data = GetImageData(image);
    	
    	for(int j = 0; j < 113; j++) {
    		for(int i = 0; i < width; i++) {
    			short datum = cthead[j][yslice][i];
    			
				assignColour(data, datum, i, j, width);
    		}
    	}
    	return image;
    	
    }
    /*
     * Side View Function - 
     * using the x axis
     */
    public BufferedImage sideView(BufferedImage image, int xslice) {
    	int width = image.getWidth();
    	int height = image.getHeight();
    	byte[] data = GetImageData(image);
    	
    	for(int j = 0; j < height; j++) {
    		for(int i = 0; i < 113; i++) {
    			short datum = cthead[i][j][xslice];
    			
				assignColour(data, datum, i, j, width);
    		}
    	}
    	return image;
    }
    	

    /*
        This function shows how to carry out an operation on an image.
        It obtains the dimensions of the image, and then loops through
        the image carrying out the copying of a slice of data into the
		image.
    */
    public BufferedImage MIP(BufferedImage image) {
            //Get image dimensions, and declare loop variables
            int w=image.getWidth(), h=image.getHeight(), i, j, c, k;
            //Obtain pointer to data for fast processing
            byte[] data = GetImageData(image);
			float col;
			short datum;
            //Shows how to loop through each pixel and colour
            //Try to always use j for loops in y, and i for loops in x
            //as this makes the code more readable
            for (j=0; j<h; j++) {
                    for (i=0; i<w; i++) {
							//at this point (i,j) is a single pixel in the image
							//here you would need to do something to (i,j) if the image size
							//does not match the slice size (e.g. during an image resizing operation
							//If you don't do this, your j,i could be outside the array bounds
							//In the framework, the image is 256x256 and the data set slices are 256x256
							//so I don't do anything - this also leaves you something to do for the assignment
							datum=cthead[76][j][i]; //get values from slice 76 (change this in your assignment)
							//calculate the colour by performing a mapping from [min,max] -> [0,255]
							col=(255.0f*((float)datum-(float)min)/((float)(max-min)));
                            for (c=0; c<3; c++) {
								//and now we are looping through the bgr components of the pixel
								//set the colour component c of pixel (i,j)
								data[c+3*i+3*j*w]=(byte) col;
                            } // colour loop
                    } // column loop
            } // row loop

            return image;
    }

    public static void main(String[] args) throws IOException {
 
       Main e = new Main();
       e.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       e.start();
    }
}