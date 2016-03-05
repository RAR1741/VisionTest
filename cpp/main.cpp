#include <iostream>
#include "opencv/cv.h"
#include "opencv/highgui.h"

using namespace cv;
using namespace std;

int main(int argc, char **argv)
{
  cout << "Initializing camera..." << endl;
  VideoCapture videoCapture;
  Mat matOriginal;
  videoCapture.open("http://10.17.0.90/mjpg/video.mjpg");
  
  videoCapture.read(matOriginal);
  
  imwrite("image.jpg", matOriginal);
  
  videoCapture.release();
  cout << "Exiting application." << endl;
}
