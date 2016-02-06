import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import edu.wpi.first.wpilibj.networktables.NetworkTable;

public class TowerTracker {
	
//	Camera settings:
//	Color level: 34
//	Brightness: 41
//	Sharpness: 12
//	Contrast: 62
//	White balance: automatic
//	Exposure value: 25
//	Exposure control: hold current
//	Enable back light compensation: true
//	Exposure zones: auto
//	Enable anonymous viewer login (no user name or password required): TRUE

	/**
	 * static method to load opencv and networkTables
	 */
	static{ 
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-1781-frc.local");
	}
//	constants for the color rbg values
	public static final Scalar 
		RED = new Scalar(0, 0, 255),
		BLUE = new Scalar(255, 0, 0),
		GREEN = new Scalar(0, 255, 0),
		BLACK = new Scalar(0,0,0),
		YELLOW = new Scalar(0, 255, 255),
//		these are the threshold values in order 
		LOWER_BOUNDS = new Scalar(58,0,109),
		UPPER_BOUNDS = new Scalar(93,255,240);
	
//		Original color values
//		LOWER_BOUNDS = new Scalar(58,0,109),
//		UPPER_BOUNDS = new Scalar(93,255,240);

//	the size for resizing the image
	public static final Size resize = new Size(640,480);

//	ignore these
	public static VideoCapture videoCapture;
	public static Mat matInput, matOriginal, matHSV, matThresh, clusters, matHeirarchy;
	public static NetworkTable table;

//	Constants for known variables
//	the height to the top of the target is 97 inches	
	public static final int TOP_TARGET_HEIGHT = 97;
//	the physical height of the camera lens
	public static final int TOP_CAMERA_HEIGHT = 32;

//	camera details, can usually be found on the data sheets of the camera
	public static final double VERTICAL_FOV  = 55;
	public static final double HORIZONTAL_FOV  = 55;
	public static final double CAMERA_ANGLE = 10;

	public static boolean shouldRun = true;
	
//	Display variables
	public static JFrame frame;
	public static JLabel lbl;
	public static ImageIcon image;

	public static void main(String[] args) {
		matOriginal = new Mat();
		matHSV = new Mat();
		matThresh = new Mat();
		clusters = new Mat();
		matHeirarchy = new Mat();
		table = NetworkTable.getTable("Targeting");
		
	    frame=new JFrame();
	    frame.setLayout(new FlowLayout());
	    frame.setSize(1350, 1000);
	    frame.setVisible(true);
		
//		main loop of the program
		while(shouldRun){
			try {
//				opens up the camera stream and tries to load it
				System.out.println("Initializing camera...");
				videoCapture = new VideoCapture();
				
				System.out.println("Opening stream...");
				videoCapture.open("http://10.17.0.90/mjpg/video.mjpg");
//				videoCapture.open("http://10.17.81.11/mjpg/video.mjpg");
				
				System.out.println("Checking connection...");
//				wait until it is opened
				while(!videoCapture.isOpened()){}
				
				System.out.println("Opened successfully...");
				
//				Actually process the image
				processImage();
				System.out.println("Finished processing...");
			} catch (Exception e) {
				System.out.println("Uh oh...");
				e.printStackTrace();
				break;
			}
		}
//		make sure the java process quits when the loop finishes
		System.out.println("Releasing video stream...");
		videoCapture.release();
		System.out.println("Exiting application...");
		System.exit(0);
	}

	public static void processImage(){
		System.out.println("Processing...");
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		double x,y,targetX,targetY,distance,azimuth;
		String output = new String();
//		only run for the specified time
		while(true){
//			Clear variables from previous loop
			contours.clear();
			output = "";
			
//			Capture image from the axis camera
			videoCapture.read(matOriginal);
			matInput = matOriginal.clone();
			
//			Load image from a static file for testing
//			matOriginal = Imgcodecs.imread("original.png");
			
			Imgproc.cvtColor(matOriginal, matHSV, Imgproc.COLOR_BGR2HSV);
			Core.inRange(matHSV, LOWER_BOUNDS, UPPER_BOUNDS, matThresh);
			Imgproc.findContours(matThresh, contours, matHeirarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

//			Make sure the contours that are detected are at least 25x25 pixels and an aspect ratio greater then 1
			for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext();) {
				MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
				Rect rec = Imgproc.boundingRect(matOfPoint);
				if(rec.height < 25 || rec.width < 25){
					iterator.remove();
					continue;
				}
				float aspect = (float)rec.width/(float)rec.height;
				if(aspect < 1.0)
					iterator.remove();
			}
			
//			Draw the contour rectangles on the material
			for(MatOfPoint mop : contours){
				Rect rec = Imgproc.boundingRect(mop);
				Imgproc.rectangle(matOriginal, rec.br(), rec.tl(), RED);
			}
			
//			Loop over each contour
			for (int p = 0; p < contours.size(); p++) {
				Rect rec = Imgproc.boundingRect(contours.get(p));

				y = rec.br().y + rec.height / 2;
				y= -((2 * (y / matOriginal.height())) - 1);
				distance = (TOP_TARGET_HEIGHT - TOP_CAMERA_HEIGHT) / 
						Math.tan((y * VERTICAL_FOV / 2.0 + CAMERA_ANGLE) * Math.PI / 180);
				
//				Angle to target
				targetX = rec.tl().x + rec.width / 2;
				targetX = (2 * (targetX / matOriginal.width())) - 1;
				azimuth = normalize360(targetX*HORIZONTAL_FOV /2.0 + 0);
				
//				Draw values on target
				Point center = new Point(rec.br().x,rec.br().y+10);
				Point centerw = new Point(rec.br().x,rec.br().y+25);
				Imgproc.putText(matOriginal, ""+(int)distance+"\"", center, Core.FONT_HERSHEY_PLAIN, 1, RED);
				Imgproc.putText(matOriginal, ""+(int)azimuth, centerw, Core.FONT_HERSHEY_PLAIN, 1, GREEN);
				
//				Build the output string to write to the network tables
				output += String.format("%d,%d%s", (int)distance, (int)azimuth, ((p==contours.size()-1)?"":"|"));
			}
			
//			Output an image for debugging
//			Imgcodecs.imwrite("output.png", matHSV);
			
//			Build the display debugging window
			frame.getContentPane().removeAll();
			image = new ImageIcon(createAwtImage(matInput));
			JLabel label1 = new JLabel(image);
			label1.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			label1.setAlignmentY(JLabel.TOP_ALIGNMENT);
			frame.getContentPane().add(label1);
			
			image = new ImageIcon(createAwtImage(matHSV));
			JLabel label2 = new JLabel(image);
			label2.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
			label2.setAlignmentY(JLabel.TOP_ALIGNMENT);
			frame.getContentPane().add(label2);
			
			image = new ImageIcon(createAwtImage(matThresh));
			JLabel label3 = new JLabel(image);
			label3.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			label3.setAlignmentY(JLabel.BOTTOM_ALIGNMENT);
			frame.getContentPane().add(label3);
			
			image = new ImageIcon(createAwtImage(matOriginal));
			JLabel label4 = new JLabel(image);
			label4.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
			label4.setAlignmentY(JLabel.BOTTOM_ALIGNMENT);
			frame.getContentPane().add(label4);
			
//			Force the display frame to update
			SwingUtilities.updateComponentTreeUI(frame);
			
//			Write the output sting to the network table
			table.putString("targets", output);
//			System.out.println(output);
		}
	}
	/**
	 * @param angle a non-normalized angle
	 */
	public static double normalize360(double angle){
//		while(angle >= 360.0)
//		{
//			angle -= 360.0;
//		}
//		while(angle < 0.0)
//		{
//			angle += 360.0;
//		}
		return angle;
	}
	
	public static BufferedImage createAwtImage(Mat mat) {

	    int type = 0;
	    if (mat.channels() == 1) {
	        type = BufferedImage.TYPE_BYTE_GRAY;
	    } else if (mat.channels() == 3) {
	        type = BufferedImage.TYPE_3BYTE_BGR;
	    } else {
	        return null;
	    }

	    BufferedImage image = new BufferedImage(mat.width(), mat.height(), type);
	    WritableRaster raster = image.getRaster();
	    DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
	    byte[] data = dataBuffer.getData();
	    mat.get(0, 0, data);

	    return image;
	}
}
