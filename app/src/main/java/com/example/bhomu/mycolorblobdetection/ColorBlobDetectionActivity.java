package com.example.bhomu.mycolorblobdetection;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
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
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class ColorBlobDetectionActivity extends Activity implements CvCameraViewListener2 {
    private static final String  TAG              = "OCVSample::Activity";

    private boolean              mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size                 SPECTRUM_SIZE;
    private Scalar               CONTOUR_COLOR;
    private Handler handler;
    private Handler handler1;
    private CameraBridgeViewBase mOpenCvCameraView;
    private int a =600;
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
        Button b1 = (Button) findViewById(R.id.red);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               //onTouched(134,116,170);
                handler = new Handler();
                Runnable runnable=new Runnable() {
                    @Override
                    public void run() {

                        onTouched(134,116,170);
                        onTouche();

                        handler.postDelayed(this,100);
                    }
                };handler.postDelayed(runnable,100);
            }
        });
        Button b2 = (Button) findViewById(R.id.blue);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onTouched(134,116,170);
                handler1 = new Handler();
                Runnable runnable1=new Runnable() {
                    @Override
                    public void run() {

                        onTouched(139,85,74);


                        handler.postDelayed(this,100);
                    }
                };handler.postDelayed(runnable1,100);
            }
        });
        Button b3 = (Button) findViewById(R.id.green);
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onTouched(134,116,170);
                handler1 = new Handler();
                Runnable runnable1=new Runnable() {
                    @Override
                    public void run() {

                        onTouched(139,85,74);


                        handler.postDelayed(this,100);
                    }
                };handler.postDelayed(runnable1,100);
            }
        });

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
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public void onTouched(int hsv_h, int hsv_s, int hsv_v) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        /*
        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
        */
        int x = 396;
        int y = 200;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
        //Toast.makeText(this, "Touch image coordinates: (" + x + ", " + y + ")");


        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;
        Toast.makeText(this, "HSV= "+mBlobColorHsv, Toast.LENGTH_LONG).show();

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mBlobColorHsv.val[0] = hsv_h;
        mBlobColorHsv.val[1] = hsv_s;
        mBlobColorHsv.val[2] = hsv_v;
        mBlobColorHsv.val[3] = 0.0;

      if(mBlobColorHsv.val[0]>120 && mBlobColorHsv.val[0]<140 && mBlobColorHsv.val[1]>90 && mBlobColorHsv.val[1]<130 &&mBlobColorHsv.val[2]>165 && mBlobColorHsv.val[2]<200 && mBlobColorHsv.val[3]>=0.0 && mBlobColorHsv.val[3]<64)
        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();


    }


        public void onTouche() {
        int cols = mRgba.cols();
        int rows = mRgba.rows();
        /*
        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;
        */
    int x = 396;
    int y = 200;

    Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");
    //Toast.makeText(this, "Touch image coordinates: (" + x + ", " + y + ")");


    Rect touchedRect = new Rect();

    touchedRect.x = (x>4) ? x-4 : 0;
    touchedRect.y = (y>4) ? y-4 : 0;

    touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
    touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

    Mat touchedRegionRgba = mRgba.submat(touchedRect);

    Mat touchedRegionHsv = new Mat();
    Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

    // Calculate average color of touched region
    mBlobColorHsv = Core.sumElems(touchedRegionHsv);
    int pointCount = touchedRect.width*touchedRect.height;
    for (int i = 0; i < mBlobColorHsv.val.length; i++)
    mBlobColorHsv.val[i] /= pointCount;
    Toast.makeText(this, "HSV= "+mBlobColorHsv, Toast.LENGTH_LONG).show();

    mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);
    Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
            ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");
    /*
    mBlobColorHsv.val[0] = hsv_h;
    mBlobColorHsv.val[1] = hsv_s;
    mBlobColorHsv.val[2] = hsv_v;
    mBlobColorHsv.val[3] = 0.0;
    */
  //  if(mBlobColorHsv.val[0]>120 && mBlobColorHsv.val[0]<140 && mBlobColorHsv.val[1]>90 && mBlobColorHsv.val[1]<130 &&mBlobColorHsv.val[2]>165 && mBlobColorHsv.val[2]<200 && mBlobColorHsv.val[3]>=0.0 && mBlobColorHsv.val[3]<64)
            mDetector.setHsvColor(mBlobColorHsv);

    Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

    mIsColorSelected = true;

    touchedRegionRgba.release();
    touchedRegionHsv.release();

            if(mBlobColorHsv.val[0] == 134 && mBlobColorHsv.val[0] ==116 && mBlobColorHsv.val[0] ==170){
                Log.i(TAG, "////////////////Stop the quadcopter ") ;
            }


}


    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();
            Log.e(TAG, "Contours count: " + contours.size());
            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);
        }

        return mRgba;
    }

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }
}
