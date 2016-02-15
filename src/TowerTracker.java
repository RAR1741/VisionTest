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

	/**
	 * static method to load opencv and networkTables
	 */
	static{ 
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		NetworkTable.setClientMode();
		NetworkTable.setIPAddress("roborio-1741-frc.local");
	}
//	Constants for RGB values
	public static final Scalar 
		RED = new Scalar(0, 0, 255),
		BLUE = new Scalar(255, 0, 0),
		GREEN = new Scalar(0, 255, 0),
		BLACK = new Scalar(0,0,0),
		YELLOW = new Scalar(0, 255, 255),
//		Lower and upper bounds of the HSV filtering
		LOWER_BOUNDS = new Scalar(73,53,220),
		UPPER_BOUNDS = new Scalar(94,255,255);

//	Main image size
	public static final Size resize = new Size(640,480);

//	Random variables
	public static VideoCapture videoCapture;
	public static Mat matInput, matOriginal, matHSV, matThresh, clusters, matHeirarchy;
	public static NetworkTable table;

//	Constants for known variables
	public static final int TOP_TARGET_HEIGHT = 97;
	public static final int TOP_CAMERA_HEIGHT = 11;

//	Camera detail constants
	public static final double VERTICAL_FOV  = 47;
	public static final double HORIZONTAL_FOV  = 47;
	public static final double CAMERA_ANGLE = 30;

//	Main loop variable
	public static boolean shouldRun = true;
	
//	Display variables
	public static JFrame frame;
	public static JLabel lbl;
	public static ImageIcon image;

	public static void main(String[] args) {
//		Initialize the matrixes
		matOriginal = new Mat();
		matHSV = new Mat();
		matThresh = new Mat();
		clusters = new Mat();
		matHeirarchy = new Mat();
		
//		Set the network table to use
		table = NetworkTable.getTable("Targeting");
		
//		Initialize the GUI
	    frame=new JFrame();
	    frame.setLayout(new FlowLayout());
	    frame.setSize(1350, 1000);
	    frame.setVisible(true);
		
//		Main loop
		while(shouldRun){
			try {
//				Opens up the camera stream and tries to load it
				System.out.println("Initializing camera...");
				videoCapture = new VideoCapture();
				
				System.out.println("Opening stream...");
				videoCapture.open("http://10.17.0.90/mjpg/video.mjpg");
				
				System.out.println("Checking connection...");
//				Wait until it is opened
				while(!videoCapture.isOpened()){}
				
				System.out.println("Opened successfully...");
				
//				Actually process the image
				processImage();
				System.out.println("Finished processing...");
			} catch (Exception e) {
//				Catch any errors and print them to the console
				System.out.println("Uh oh...");
				e.printStackTrace();
				break;
			}
		}
//		Exit the Java process when the loop finishes
		System.out.println("Releasing video stream...");
		videoCapture.release();
		System.out.println("Exiting application...");
		System.exit(0);
	}

	public static void processImage(){
		System.out.println("Processing...");
		ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		double x,y,targetX,targetY,distance,pan,tilt;
		String output = new String();

		while(true){
//			Clear variables from previous loop
			contours.clear();
			output = "";
			
//			Capture image from the axis camera
			videoCapture.read(matOriginal);
			matInput = matOriginal.clone();
			
//			Convert the image type from BGR to HSV
			Imgproc.cvtColor(matOriginal, matHSV, Imgproc.COLOR_BGR2HSV);
			
//			Filter out any colors not inside the threshold
			Core.inRange(matHSV, LOWER_BOUNDS, UPPER_BOUNDS, matThresh);
			
//			Find the contours
			Imgproc.findContours(matThresh, contours, matHeirarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			
//			Draw the initial contour rectangles on the material
			for(MatOfPoint mop : contours){
				Rect rec = Imgproc.boundingRect(mop);
				drawContour(matOriginal, rec, RED);
			}

//			Make sure the contours that are detected are at least 30x30 pixels and a valid aspect ratio
			for (Iterator<MatOfPoint> iterator = contours.iterator(); iterator.hasNext();) {
				MatOfPoint matOfPoint = (MatOfPoint) iterator.next();
				Rect rec = Imgproc.boundingRect(matOfPoint);
				if(rec.height < 30 || rec.width < 30){
					iterator.remove();
					drawContour(matOriginal, rec, BLUE);
					continue;
				}
				
				float aspect = (float)rec.width/(float)rec.height;
				if(aspect > 2.0 || aspect < 1){
					drawContour(matOriginal, rec, YELLOW);
					iterator.remove();
				}
			}
			
//			Draw the final contour rectangles on the material
			for(MatOfPoint mop : contours){
				Rect rec = Imgproc.boundingRect(mop);
				drawContour(matOriginal, rec, GREEN);
			}
			
//			Calculate targeting output for each of the remaining contours
			for (int p = 0; p < contours.size(); p++) {
				Rect rec = Imgproc.boundingRect(contours.get(p));

				y = rec.br().y + (rec.height / 2);
				y = -((2 * (y / matOriginal.height())) - 1);
				
				distance = (TOP_TARGET_HEIGHT - TOP_CAMERA_HEIGHT) / 
						Math.tan((y * VERTICAL_FOV / 2.0 + CAMERA_ANGLE) * Math.PI / 180);
				
//				Horizontal angle to target
				targetX = rec.tl().x + rec.width / 2;
				targetX = (2 * (targetX / matOriginal.width())) - 1;
				pan = -(targetX*HORIZONTAL_FOV /2.0);
				
//				Vertical angle to target
				targetY = rec.tl().y + rec.height / 2;
				targetY = (2 * (targetY / matOriginal.height())) - 1;
				tilt = -(targetY*VERTICAL_FOV /2.0);
				
//				Draw values on target
				Point center = new Point(rec.br().x,rec.br().y+10);
				Point centerw = new Point(rec.br().x,rec.br().y+25);
				Imgproc.putText(matOriginal, ""+(int)pan, center, Core.FONT_HERSHEY_PLAIN, 1, RED);
				Imgproc.putText(matOriginal, ""+(int)tilt, centerw, Core.FONT_HERSHEY_PLAIN, 1, GREEN);
				
//				Build the output string to write to the network tables
				output += String.format("%d,%d%s", (int)pan, (int)tilt, ((p==contours.size()-1)?"":"|"));
			}
			
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
	
//	Draws the supplied rectangle on the supplied material using the supplied color
	public static void drawContour(Mat drawMat, Rect tempRect, Scalar color){
		Imgproc.rectangle(drawMat, tempRect.br(), tempRect.tl(), color);
	}
	
//	Takes in a matrix and outputs an image useable by the GUI
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
