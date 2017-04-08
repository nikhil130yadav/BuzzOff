package hymob.developers.inspiron.buzzoff2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity  implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        if (OpenCVLoader.initDebug()) {
            Log.i("openCv", "OpenCV initialize success");
           // new BaseLoaderCallback(getApplications).onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.i("OpenCV", "OpenCV initialize failed");
        }
    }



    /*
    **FOR permission
     */
    private static final int CAMERA_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    Handler handler = new Handler();

    //
    Button startButtonswipe;

    private boolean START_FACE_RECOGNITION = false;

    private JavaCameraView javaCameraView;
    private int mAbsoluteFaceSize;
    private static final float FACE_SIZE_PERCENTAGE = 0.3f;
    LinearLayoutCompat linearLayoutCompat;
    CoordinatorLayout coordinatorLayout;
    Toolbar toolbar;
    //******for vibrate*******//
    Vibrator vibrator;
    //**********for ringtone**********//
    MediaPlayer mMediaPlayer;
    Uri notifcation;
    Ringtone r;
    ImageButton imageButton;
    TextView facesText;
    TextView eyesText;
    private Toast toast;
    private Mat mRgba;
    private Mat mGrey;
    private Point mPt1 = new Point();
    private Point mPt2 = new Point();
    private File mcascadeFile;
    private int mPreviousEyesState = -1;
    private boolean mIsEyeCLosingDetected = false;
    private boolean mIsDetecionOn = false;
    private CascadeClassifier haarcascade, mEyesCascade;
    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                   //  Toast.makeText(getApplicationContext(),"conneceted",Toast.LENGTH_SHORT).show();

                    try {
                        {
                            //************loading front face cascade**************//
                            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            mcascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                            FileOutputStream os = new FileOutputStream(mcascadeFile);
                            byte[] buffer = new byte[4096];
                            int byteread;
                            while ((byteread = is.read(buffer)) != -1) {
                                os.write(buffer, 0, byteread);
                            }
                            is.close();
                            os.close();
                            //********8haarcascade object of cascade classifier***************//
                            haarcascade = new CascadeClassifier(mcascadeFile.getAbsolutePath());
                            if (haarcascade.empty()) {
                                Log.i("Cascadeface ", "unable to load cascade");
                                haarcascade = null;

                            }
                        }
                        // Second URL
                        //*****loading eye cascade*****///
                        {
                            // Copy the resource into a temp file so OpenCV can load it
                            InputStream is = getResources().openRawResource(R.raw.haarcascade_eye_tree_eyeglasses);
                            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                            File mCascadeFile = new File(cascadeDir, "haarcascade_eye_tree_eyeglasses.xml");
                            FileOutputStream os = new FileOutputStream(mCascadeFile);


                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                            is.close();
                            os.close();

                            // Load the cascade classifier
                            mEyesCascade = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                            if (mEyesCascade.empty()) {
                                Log.i("CascadeEye ", "unable to load cascade");
                                haarcascade = null;

                            }
                        }


                    } catch (Exception e) {
                        Log.i("cacased ERROR", "casecasde not found");

                    }
                  //  javaCameraView.enableView();
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ){
                       // Toast.makeText(getApplicationContext(),"camera permsiison needed",Toast.LENGTH_SHORT).show();

                       // askPermission();
                    }else{
                        Log.d("opencv","camera view");
                        //*****enabling camera*******//
                      javaCameraView.enableView();
                    }

                    mIsDetecionOn = true;
                    break;
                default:
                    super.onManagerConnected(status);
            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        linearLayoutCompat = (LinearLayoutCompat) findViewById(R.id.linearlayoutcomp);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.root_layout);
        facesText = (TextView) findViewById(R.id.face_textview);
        eyesText = (TextView) findViewById(R.id.eye_textview);
        toolbar = (Toolbar) findViewById(R.id.toolbar);



        startButtonswipe = (Button) findViewById(R.id.startButtonSwipe);
        startButtonswipe.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this){
            public void onSwipeRight(){
                //  Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                startButtonswipe.setText("swipe left to stop");
                startButtonswipe.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_left_arrow,0,0,0);

                startButtonswipe.setBackgroundColor(getResources().getColor(R.color.red));
                START_FACE_RECOGNITION = true;


            }
            public void onSwipeLeft(){
               // Toast.makeText(MainActivity.this, "right", Toast.LENGTH_SHORT).show();
                startButtonswipe.setText("swipe right to start");
                startButtonswipe.setBackgroundColor(getResources().getColor(R.color.green));
                startButtonswipe.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_right_arrow,0);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        facesText.setText("Faces: 0");
                        eyesText.setText("Eyes: 0");
                    }
                });

                try {
                    if (mMediaPlayer != null) {
                        if (mMediaPlayer.isPlaying()){

                            mMediaPlayer.pause();
                         //   mMediaPlayer.release();
                        }
                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }

                START_FACE_RECOGNITION = false;

            }
        });



        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
  //      notifcation = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
//        r = RingtoneManager.getRingtone(getApplicationContext(), notifcation);

        //========== asking permision for CAMERA ==================================
        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            askPermission();
            javaCameraView = (JavaCameraView) findViewById(R.id.java_surface_view);
            proceedAfterPermission();
        }else{
            javaCameraView = (JavaCameraView) findViewById(R.id.java_surface_view);
            proceedAfterPermission();
            AudioManager mobilemode = (AudioManager)getBaseContext().getSystemService(Context.AUDIO_SERVICE);
//   int streamMaxVolume = mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING);
            switch (mobilemode.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:


                    mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:

                    mobilemode.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    mobilemode.setStreamVolume(AudioManager.STREAM_RING,mobilemode.getStreamMaxVolume(AudioManager.STREAM_RING),0);
                    break;
                case AudioManager.RINGER_MODE_NORMAL:

                    break;
            }
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer = MediaPlayer.create(this, R.raw.warning_ringtone);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            // mMediaPlayer.setLooping(true);
        }
        //=========================================================================




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ring stop
                try {
                    if (mMediaPlayer.isPlaying()) {
                        //r.stop();
                        mMediaPlayer.pause();
                        //  mMediaPlayer.release();

                    }
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }
//**** making screen fullscreen*****//

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.warning_ringtone);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //  OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseLoaderCallback);
        if (OpenCVLoader.initDebug()) {

            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);


          /*  if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
            }else{
          //      baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }*/

            // new BaseLoaderCallback(getApplications).onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Snackbar.make(coordinatorLayout,"ok",Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mGrey = new Mat(height, width, CvType.CV_8U);
        mAbsoluteFaceSize = (int) (height * FACE_SIZE_PERCENTAGE);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //rotating imge
        mRgba = inputFrame.rgba();
        // mGrey=inputFrame.gray();
        Imgproc.cvtColor(mRgba, mGrey, Imgproc.COLOR_RGBA2RGB);

        //Core.flip(mRgba,mRgba,1);

        //*****detecting faces*****//
        MatOfRect faces = new MatOfRect();
        //

        Size mMinSize = new Size(mAbsoluteFaceSize, mAbsoluteFaceSize);
        Size mMaxSize = new Size();
        //
        if (haarcascade != null) {
            // haarcascade.detectMultiScale(mGrey,faces,1.1,2,2,new Size(200,200),new Size());
            haarcascade.detectMultiScale(mGrey, faces, 1.1, 2, 2, mMinSize, mMaxSize);
        }
        final Rect[] facesArray = faces.toArray();
        if (facesArray.length != 0 && START_FACE_RECOGNITION) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    facesText.setText("Faces: "+facesArray.length);
                }
            });
        }
        for (int i = 0; i < facesArray.length; i++) {

            if(START_FACE_RECOGNITION){
                //imgproc.rectangele () method drws rectnagle around the face
                Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(100, 23, 1, 1), 3);


                //***********Detecting out eyes from faces*******************//
                Mat faceROI = mGrey.submat(facesArray[i]);
                //recnet change
                Mat faceROI2 = mRgba.submat(facesArray[i]);

                final MatOfRect eyes = new MatOfRect();
                // in each face detect
                if (mEyesCascade != null) {
                    mEyesCascade.detectMultiScale(faceROI, eyes, 1.1, 2, 2, new Size(30, 30), new Size());
                    // mEyesCascade.detectMultiScale(faceROI, eyes, 1.1, 2, 2, mMinSize, mMaxSize);
                   // Log.i("eyescascades", "scaled");
                }
                final Rect[] eyesArray = eyes.toArray();
                //to draw rectgange around the eyes
                for(int j= 0; j< eyesArray.length;j++){
                    if (START_FACE_RECOGNITION) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {

                                eyesText.setText("Eyes:"+eyesArray.length);
                                eyesText.setTextColor(getResources().getColor(R.color.white));
                            }
                        });
                    }
                    Imgproc.rectangle(faceROI2,eyesArray[j].tl(),eyesArray[j].br(),new Scalar(100,23,1,1),3);
                }
                // for eye closed
                if (eyesArray.length == 0 && mPreviousEyesState == 0) {


                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            eyesText.setText("Eyes: Closed ");
                            eyesText.setTextColor(getResources().getColor(R.color.red));
                        }
                    });

                    //****for starting vibration****//
                    try {
                       handler.post(new Runnable() {
                           @Override
                           public void run() {
                               try {
                                   vibrator.vibrate(1000);
                                   // r.play();

                                   mMediaPlayer.start();
                               } catch (IllegalStateException e) {
                                   e.printStackTrace();
                               }
                           }
                       }) ;

                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }




                } else if (eyesArray.length == 2 && mPreviousEyesState == 2) {
                /*if (r.isPlaying()) {
                    r.stop();

                }*/
                    //mMediaPlayer.stop();
                   handler.post(new Runnable() {
                       @Override
                       public void run() {

                           try {
                               if (mMediaPlayer.isPlaying()){
                                   mMediaPlayer.pause();
                                   // mMediaPlayer.release();
                               }
                           } catch (IllegalStateException e) {
                               e.printStackTrace();
                           }
                       }
                   });


                }

                //code for eye blink
            /*
            if (eyesArray.length<2 && mPreviousEyesState==2){

                mIsEyeCLosingDetected=true;


            }else if (eyesArray.length==2&& mIsEyeCLosingDetected && mIsDetecionOn){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //toast.cancel();
                        toast=Toast.makeText(getApplicationContext(),"detected",Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
                mIsEyeCLosingDetected=false;
            }*/

                mPreviousEyesState = eyesArray.length;
            }
        }
        return mRgba;
    }




    private void proceedAfterPermission(){
      //  Toast.makeText(MainActivity.this,"got permission",Toast.LENGTH_SHORT).show();
        javaCameraView.setVisibility(SurfaceView.VISIBLE);

        javaCameraView.setCvCameraViewListener(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer = MediaPlayer.create(this, R.raw.warning_ringtone);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        Log.i("OpenCV", "pap enc");
    };
/*
**method to ask permission
 */

    private  void askPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA)) {
            //Show Information about why you need the permission

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);

      /*      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Need Storage Permission");
            builder.setMessage("This app needs storage permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();*/
        } else if (permissionStatus.getBoolean(Manifest.permission.CAMERA,false)) {
            /*//Previously Permission Request was cancelled with 'Dont Ask Again',
            // Redirect to Settings after showing Information about why you need the permission
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Need Camera Permission");
            builder.setMessage("This app needs camera permission.");
            builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    sentToSettings = true;
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                    Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();*/
        } else {
            //just request the permission
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
        }


    /*    SharedPreferences.Editor editor = permissionStatus.edit();
        editor.putBoolean(Manifest.permission.CAMERA,true);
        editor.commit();*/

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CONSTANT) {
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(Manifest.permission.CAMERA,true);
                editor.commit();
                //The External Storage Write Permission is granted to you... Continue your left job...
                try {//OpenCVLoader.initDebug();

                    javaCameraView.findViewById(R.id.java_surface_view);
                    javaCameraView.enableView();
                    proceedAfterPermission();

                } catch (Exception e) {
                    e.printStackTrace();
                }}
            else {
                    Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
                }
            }
        }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission

                proceedAfterPermission();
            }
        }
    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (sentToSettings) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                //Got Permission
                proceedAfterPermission();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mMediaPlayer != null){
            mMediaPlayer.release();

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.help) {
            startActivity(new Intent(MainActivity.this,HelpActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
