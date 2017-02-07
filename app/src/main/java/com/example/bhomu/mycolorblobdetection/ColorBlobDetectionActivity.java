package com.example.bhomu.mycolorblobdetection;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.List;

import static org.opencv.imgproc.Imgproc.contourMoments;

public class ColorBlobDetectionActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

   // private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
  //  private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
   // private Mat                  mSpectrum;
  //  private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private Handler handler;

    private CameraBridgeViewBase mOpenCvCameraView;
    private int hsv_h=250,hsv_s=162,hsv_v=198;
    private double mCameraViewWidth;
    private double mCameraViewHeight;
    private double mCameraViewCentreX=640;
    private double getmCameraViewCentreY=360;
    private double centreX,centreY;
    int flag,count;
    TextView textView1,textView2;
    public String s1="color not detected",s2="color not detected";

    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();


                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public ColorBlobDetectionActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_color_blob_detection);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.color_blob_detection_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        if (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            Log.d(TAG, "Everything should be fine with using the camera.");
        } else {
            Log.d(TAG, "Requesting permission to use the camera.");
            String[] CAMERA_PERMISSONS = {
                    Manifest.permission.CAMERA
            };
            ActivityCompat.requestPermissions(this, CAMERA_PERMISSONS, 0);
        }

        Button b1 = (Button) findViewById(R.id.red);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hsv_h=250;
                hsv_s=162;
                hsv_v=198;
                onTouched();


            }
        });
        Button b2 = (Button) findViewById(R.id.blue);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hsv_h=164;
                hsv_s=124;
                hsv_v=150;
                onTouched();


            }
        });
        Button b3 = (Button) findViewById(R.id.green);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hsv_h=77;
                hsv_s=172;
                hsv_v=147;
                onTouched();


            }
        });
        Button b4 = (Button) findViewById(R.id.yellow);
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hsv_h=42;
                hsv_s=203;
                hsv_v=199;
                onTouched();


            }
        });

        handler = new Handler();
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {

                        textView1=(TextView)findViewById(R.id.upDown);
                        textView2=(TextView)findViewById(R.id.leftRight);
                        textView1.setText(s1);
                        textView2.setText(s2);



                        handler.postDelayed(this,100);
                    }
                };handler.postDelayed(runnable,100);
 //       controlSignal();

    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();

    }


    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
 //       mSpectrum = new Mat();
  //      mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
//        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
        mCameraViewWidth=(double)width;
        mCameraViewHeight=(double)height;
  //      mBlobColorHsv.val[0] = hsv_h;
//        mBlobColorHsv.val[1] = hsv_s;
//        mBlobColorHsv.val[2] = hsv_v;
//        mBlobColorHsv.val[3] = 0.0;
//        mDetector.setHsvColor(mBlobColorHsv);

    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public void onTouched() {
        mBlobColorHsv.val[0] = hsv_h;
        mBlobColorHsv.val[1] = hsv_s;
        mBlobColorHsv.val[2] = hsv_v;
        mBlobColorHsv.val[3] = 0.0;
        mDetector.setHsvColor(mBlobColorHsv);
        Toast.makeText(this,"x = "+centreX+ "  y = "+centreY,Toast.LENGTH_LONG).show();
//        int cols = mRgba.cols();
//        int rows = mRgba.rows();
//        /*
//        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
//        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
//        */
//        int x = 396;
//        int y = 200;
//
//        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
//        Toast.makeText(this, "Touch image coordinates: (" + x + ", " + y + ")");
//
//
//        Rect touchedRect = new Rect();
//
//        touchedRect.x = (x>4) ? x-4 : 0;
//        touchedRect.y = (y>4) ? y-4 : 0;
//
//        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
//        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;
//
//        Mat touchedRegionRgba = mRgba.submat(touchedRect);
//
//        Mat touchedRegionHsv = new Mat();
//        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);
//
//        // Calculate average color of touched region
//        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
//        int pointCount = touchedRect.width*touchedRect.height;
//        for (int i = 0; i < mBlobColorHsv.val.length; i++)
//            mBlobColorHsv.val[i] /= pointCount;
//        Toast.makeText(this, "HSV= "+mBlobColorHsv, Toast.LENGTH_LONG).show();
//
//        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
//        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
//                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

//        mBlobColorHsv.val[0] = hsv_h;
//        mBlobColorHsv.val[1] = hsv_s;
//        mBlobColorHsv.val[2] = hsv_v;
//        mBlobColorHsv.val[3] = 0.0;
//
//        mDetector.setHsvColor(mBlobColorHsv);

       // Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

//        mIsColorSelected = true;

//        touchedRegionRgba.release();
//        touchedRegionHsv.release();

    }



    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

       // if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            count=contours.size();
        Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

        double[] colorResult = new double[2];
        final boolean colorFound = findcolor(contours, colorResult);
        final double leftRightLocation = colorResult[0]; // -1 for left ...  1 for right
        final double topBottomLocation = colorResult[1]; // 1 for top ... 0 for bottom
        if (colorFound) {
            // Draw a circle on the screen at the center.
            double colorCenterX = topBottomLocation * mCameraViewWidth;
            double colorCenterY = (leftRightLocation + 1.0) / 2.0 * mCameraViewHeight;
            Imgproc.circle(mRgba, new Point(colorCenterX, colorCenterY), 5, CONTOUR_COLOR, -1);
            Log.i(TAG, "//////////////  x = " + colorCenterX+"           y = "+colorCenterY);
            centreX=colorCenterX;
            centreY=colorCenterY;
            if((centreX-750) > 40){
                Log.i(TAG," /////////////////////Move right ");
                s1="Move back";


            }
            else {
                if((centreX-750) < -40) {
                    Log.i(TAG, " /////////////////////Move left ");
                    s1="Move forward";

                }
                else{
                    Log.i(TAG,"/////////////////Stop");
                    s1="Stop";

                }
            }
            if((centreY-360) > 25){
                Log.i(TAG,"//////////////////Move down");
                s2="Move left";


            }
            else {
                if((centreY-360) < -25) {
                    Log.i(TAG, "/////////////////// Move up ");
                    s2="Move right";

                }
                else {
                    Log.i(TAG, " //////////////Stop");
                    s2="Stop";

                }}
                    //Toast.makeText(this,"x = "+colorCenterX+ "  y = "+colorCenterY,Toast.LENGTH_LONG).show();
        }else { s1="color not detected";s2="color not detected";}

//                Log.i(TAG, "///////// Maxx = " + maxX + " MAxy = " + maxY);




//            MatOfPoint largestContour = contours.get(0);
//        double largestArea = Imgproc.contourArea(largestContour);
//        for (int i = 1; i < contours.size(); ++i) {
//            MatOfPoint currentContour = contours.get(0);
//            double currentArea = Imgproc.contourArea(currentContour);
//            if (currentArea > largestArea) {
//                largestArea = currentArea;
//                largestContour = currentContour;
//            }
       // }
//            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);
//
//            Moments moments=contourMoments(largestContour);
//
//            double aveX = moments.get_m10() / moments.get_m00();
//            double aveY = moments.get_m01() / moments.get_m00();
//            double centreX=aveX*mCameraViewWidth;
//            double centreY=(aveY+1)/2*mCameraViewHeight;
//            x=centreX;
//            y=centreY;
//
//            Imgproc.circle(mRgba,new Point(centreX,centreY),20,new Scalar(255,49,49,255));

//            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
//            colorLabel.setTo(mBlobColorRgba);
//
//            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
//            mSpectrum.copyTo(spectrumLabel);



  //      }
//        final boolean Colorfound=findcolor(contours1,ColorResult);
//        if(Colorfound){
//            double centreX=ColorResult[0]*mCameraViewWidth;
//            double centreY=(ColorResult[1]+1)/2*mCameraViewHeight;
//           // Toast.makeText(this,"x="+centreX+"y="+centreY,Toast.LENGTH_LONG).show();
//            Imgproc.circle(mRgba,new Point(centreX,centreY),5,new Scalar(255,49,0,255));
//        }

        return mRgba;
    }
    private boolean findcolor(List<MatOfPoint> contours,double[] colorResult) {
        // Step #0: Determine if any contour regions were found that match the target color criteria.
        if (contours.size() == 0) {
            return false; // No contours found.
        }

        // Step #1: Use only the largest contour. Other contours (potential other colors) will be ignored.
        MatOfPoint largestContour = contours.get(0);
        double largestArea = Imgproc.contourArea(largestContour);
        for (int i = 1; i < contours.size(); ++i) {
            MatOfPoint currentContour = contours.get(0);
            double currentArea = Imgproc.contourArea(currentContour);
            if (currentArea > largestArea) {
                largestArea = currentArea;
                largestContour = currentContour;
            }
        }




        // Step #3: Calculate the center of the blob.
        // Moments moments = Imgproc.moments(largestContour, false);
        // yep, the line above fails.  Comment out the line above and uncomment the line below.  For more info visit this page https://github.com/Itseez/opencv/issues/5017
        Moments moments = contourMoments(largestContour);
        double aveX = moments.get_m10() / moments.get_m00();
        double aveY = moments.get_m01() / moments.get_m00();
//        x=aveX;
//        y=aveY;

        // Step #4: Convert the X and Y values into leftRight and topBottom values.
        // X is 0 on the left (which is really the bottom) divide by width to scale the topBottomLocation
        // Y is 0 on the top of the view (object is left of the robot) divide by height to scale
        double leftRightLocation = aveY / (mCameraViewHeight / 2.0) - 1.0;
        double topBottomLocation = aveX / mCameraViewWidth;

        // Step #5: Populate the results array.
        colorResult[0] = leftRightLocation;
        colorResult[1] = topBottomLocation;

        return true;
    }
//    private boolean findcolor(List<MatOfPoint> contours,  double[] ColorResult) {
//        // Step #0: Determine if any contour regions were found that match the target color criteria.
//
//         if (contours.size() == 0) {
//            return false; // No contours found.
//        }
//
//        // Step #1: Use only the largest contour. Other contours (potential other colors) will be ignored.
//        MatOfPoint largestContour = contours.get(0);
//        double largestArea = Imgproc.contourArea(largestContour);
//        for (int i = 1; i < contours.size(); ++i) {
//            MatOfPoint currentContour = contours.get(0);
//            double currentArea = Imgproc.contourArea(currentContour);
//            if (currentArea > largestArea) {
//                largestArea = currentArea;
//                largestContour = currentContour;
//            }
//        }
//
////        // Step #2: Determine if this target meets the size requirement.
////        double sizePercentage = largestArea / mCameraViewArea;
////        if (sizePercentage < minSizePercentage) {
////            return false; // No color found meeting the size requirement.
////        }
//
//        // Step #3: Calculate the center of the blob.
//
//        //Moments moments = Imgproc.moments(largestContour, false);
//        // yep, the line above fails.  Comment out the line above and uncomment the line below.  For more info visit this page https://github.com/Itseez/opencv/issues/5017
//        Moments moments = contourMoments(largestContour);
//        double aveX = moments.get_m10() / moments.get_m00();
//        double aveY = moments.get_m01() / moments.get_m00();
//
//        // Step #4: Convert the X and Y values into leftRight and topBottom values.
//        // X is 0 on the left (which is really the bottom) divide by width to scale the topBottomLocation
//        // Y is 0 on the top of the view (object is left of the robot) divide by height to scale
//
//
//        // Step #5: Populate the results array.
//       ColorResult [0] =aveX;
//        ColorResult[1] = aveY;
//
//        return true;
//    }




//    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
//        Mat pointMatRgba = new Mat();
//        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
//        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);
//
//        return new Scalar(pointMatRgba.get(0, 0));
//    }
//    public void controlSignal(){
//
//        if(count!=0 ){
//            flag=1;
//            Toast.makeText(this," Color DETECTED",Toast.LENGTH_SHORT).show();
//        }else{
//            flag=0;
//            Toast.makeText(this," Color NOT DETECTED",Toast.LENGTH_SHORT).show();
//        }
//    }
}
