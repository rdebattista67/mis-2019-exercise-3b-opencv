package mis.example.misopencv;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static org.opencv.imgproc.Imgproc.warpAffine;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase    mOpenCvCameraView;
    private boolean                 mIsJavaCamera = true;
    private MenuItem                mItemSwitchCamera = null;
    private CascadeClassifier       face_cascade;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                    face_cascade  = new CascadeClassifier(initAssetFile("haarcascade_frontalface_default.xml"));

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        // before opening the CameraBridge, we need the Camera Permission on newer Android versions

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x123);
        } else {
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }
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
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat col  = inputFrame.rgba();

        //https://stackoverflow.com/a/12998670/6118088
        Mat rot = Imgproc.getRotationMatrix2D(new Point(col.cols() * 0.5,col.rows() * 0.5),270,1.0);
        Mat dst = new Mat();
        Imgproc.warpAffine(col, dst, rot, new Size(col.cols(), col.rows()));

        //https://docs.opencv.org/3.4.1/d7/d8b/tutorial_py_face_detection.html
        MatOfRect faceObj = new MatOfRect();
        //https://answers.opencv.org/question/98171/flags-parameter-in-the-detectmultiscale-function/
        face_cascade.detectMultiScale(dst, faceObj, 1.05, 5, 0, new Size(50, 50), new Size(200, 200));

        for(Rect faceRect : faceObj.toArray()){
           //https://www.programcreek.com/java-api-examples/?class=org.opencv.imgproc.Imgproc&method=circle
            //https://docs.opencv.org/2.4/doc/tutorials/core/basic_geometric_drawing/basic_geometric_drawing.html
            double xCenter = faceRect.x + faceRect.width * 0.5;
            double yCenter = faceRect.y + faceRect.height * 0.55;
            int radius = (int) Math.round(faceRect.height * 0.1);
            Imgproc.circle(dst, new Point(xCenter, yCenter), radius, new Scalar(255, 0, 0), -1);
        }

        return dst;
    }


    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getAssets().open(filename);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data); os.write(data); is.close(); os.close();
        } catch (IOException e) { e.printStackTrace(); }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getAbsolutePath();
    }
}
