package org.olimar.klog;

import org.apache.http.conn.util.InetAddressUtils;

import java.lang.System;
import java.io.File;
import java.lang.Thread;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.Date;
import java.util.Properties;
import java.util.Enumeration;
import java.util.List;
import java.nio.ByteBuffer;

import android.content.*;
import android.view.SurfaceView;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.hardware.Camera;
import android.os.Environment;
import android.media.MediaRecorder;
import android.media.CamcorderProfile;
import android.view.SurfaceHolder;
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.app.*;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.view.*;
import android.os.Bundle;
import android.widget.*;
import android.hardware.*;
import android.hardware.Camera.PreviewCallback;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.content.Context;
import android.os.Handler;
import android.media.AudioFormat;
import android.media.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.CheckBox;


/** The entry level activity for the application.
 *
 * This starts and runs the main interface to the app.  The interface allows the
 * user to start and stop the camera capture service, the data upload service,
 * and the command service.
 */
public class MainActivity extends Activity implements View.OnTouchListener
{
    final Activity mainActivity = this;
    private CheckBox chkCameraService = null;
    private final static String TAG = "KLOG";
    private Camera camera = null;
    /** Decides which video capture mode to use; false means picture-based, and
     * true means video-based. */
    boolean captureMode = false;
    private Button btnExit = null;
    private RadioButton rdoCaptureMode = null;
    private CameraService mBoundService = null;


    /** Called when the activity is first created.
     *
     * Sets up window settings and listeners.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // Window settings.
        Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.main);

        // Checkbox and button listeners.
        chkCameraService = (CheckBox) findViewById(R.id.chkCameraService);
        chkCameraService.setOnClickListener(onCameraServiceClk);
        chkCameraService = (CheckBox) findViewById(R.id.chkUploadService);
        chkCameraService.setOnClickListener(onUploadServiceClk);
        btnExit = (Button)findViewById(R.id.btn_exit);
        btnExit.setOnClickListener(exitAction);
        rdoCaptureMode = (RadioButton)findViewById(R.id.radio_pic_mode);
        if (!captureMode)
            rdoCaptureMode.toggle();

        Log.d(TAG, "onCreate exit");
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "destroy start");
        super.onDestroy();
        Log.d(TAG, "destroy exit");
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d(TAG, "onStart.");
        Log.d(TAG, "onStart.");
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "Resume start");
        Log.d(TAG, "Resume exit");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause.");

        if (camera != null)
        {
            camera.release();
            camera = null;
        }

        Log.d(TAG, "onPause before finish");
        finish();
        Log.d(TAG, "onPause after finish.");
    }


    /** Called when the "activate camera service" checkbox is clicked.
     *
     * If the service is not running, this starts it; if it is already running,
     * this stops it.
     */
    private OnClickListener onCameraServiceClk = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Log.d(TAG, "CameraServiceCLK start.");

            // checked
            if (((CheckBox) v).isChecked())
            {
                Log.d(TAG, "Camera service checkbox checked.");
                Log.d(TAG, "Intending to start Camera service..");

                if (!isServiceRunning(CameraService.class.getName()))
                {
                    // Start the service.
                    Intent intent = new Intent(mainActivity, CameraService.class);
                    ComponentName cn = startService(intent);
                    if (cn != null)
                        Log.d(TAG, "Started camera service: " + cn.toString());
                    else
                        Log.d(TAG, "Service does not exist.");

                    // Once started, tell it about the camera instance.
                    if (getApplicationContext().bindService(intent, mCameraServiceConnection, BIND_AUTO_CREATE))
                    {
                        Log.d(TAG, "Bound to service.");
                        unbindService(mCameraServiceConnection);
                        Log.d(TAG, "Undbinded from service.");
                    }
                    else
                    {
                        Log.d(TAG, "Could not bind to service.");
                    }
                }
                else
                {
                    Log.d(TAG, "Camera service already running.");
                }
            }

            // unchecked
            else
            {
                if (isServiceRunning(CameraService.class.getName()))
                {
                    Log.d(TAG, "Camera service checkbox unchecked.");
                    Intent intent = new Intent(mainActivity, CameraService.class);
                    if (stopService(intent))
                        Log.d(TAG, "Stopped camera service.");
                    else
                        Log.d(TAG, "Could not stop camera service.");
                }
                else
                {
                    Log.d(TAG, "Camera service not already running.");
                }
            }

            // done
            Log.d(TAG, "CameraServiceCLK close.");
        }
    };

    /** Called when the "activate upload service" checkbox is clicked.
     *
     * If the service is not running, this starts it; if it is already running,
     * this stops it.
     */
    private OnClickListener onUploadServiceClk = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            // was checked
            if (((CheckBox) v).isChecked())
            {
                Log.d(TAG, "Upload service checkbox checked.");

                if (!isServiceRunning(UploadService.class.getName()))
                {
                    Intent intent = new Intent(mainActivity, UploadService.class);
                    ComponentName cn = startService(intent);
                    if (cn != null && isServiceRunning(UploadService.class.getName()))
                        Log.d(TAG, "Started upload service: " + cn.toString());
                    else
                        Log.d(TAG, "Could not start upload service.");
                }
                else
                {
                    Log.d(TAG, "Upload service already running.");
                }
            }

            // was unchecked
            else
            {
                Log.d(TAG, "Upload service checkbox unchecked.");

                if (isServiceRunning(UploadService.class.getName()))
                {
                    Log.d(TAG, "Upload service checkbox unchecked.");
                    Intent intent = new Intent(mainActivity, UploadService.class);
                    stopService(intent);
                    if (!isServiceRunning(UploadService.class.getName()))
                        Log.d(TAG, "Stopped upload service.");
                    else
                        Log.d(TAG, "Could not stop upload service.");
                }
                else
                {
                    Log.d(TAG, "Upload service not already running.");
                }
            }
        }
    };

    private OnClickListener exitAction = new OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            Log.d(TAG, "exitAction");
            onPause();
            Log.d(TAG, "exitAction");
        }
    };

    public void onRadioButtonClicked(View view)
    {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch (view.getId())
        {
        case R.id.radio_pic_mode:
            if (checked)
                // Pirates are the best
                break;
        case R.id.radio_video_mode:
            if (checked)
                // Ninjas rule
                break;
        }
    }

    /** Create a File for saving the image */
    private static File getOutputMediaFile()
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_PICTURES), "MyCameraApp");

        if (! mediaStorageDir.exists())
        {
            if (! mediaStorageDir.mkdirs())
            {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                             "IMG_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    /** Create a File for saving the video. */
    private static File getOutputMediaFileVideo()
    {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                                            Environment.DIRECTORY_DCIM), "MyCameraApp");

        if (! mediaStorageDir.exists())
        {
            if (! mediaStorageDir.mkdirs())
            {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                             "MOV_"+ timeStamp + ".jpg");

        return mediaFile;
    }

    /** Starts capturing via taking videos. */
    private void startCapture2()
    {
        Log.d(TAG, "Starting camera service.");
        camera = getCameraInstance();
        if (camera == null)
        {
            return;
        }

        camera.unlock();
        MediaRecorder mr = new MediaRecorder();
        mr.setCamera(camera);

        mr.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mr.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mr.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        //mr.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO));
        mr.setOutputFile(getOutputMediaFileVideo().toString());

        //SurfaceView cameraSurface = (SurfaceView)findViewById(R.id.surface_camera);
        //mr.setPreviewDisplay(cameraSurface);

        try
        {
            mr.prepare();
        }
        catch (Exception e)
        {
            Log.e(TAG, "MediaRecorder.prepare() failed: " + e);
            return;
        }

        mr.start();
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            Log.d(TAG, "" + e);
            return;
        }
        mr.stop();
        //mr.reset();

        mr.release();

        camera.lock();
        camera.release();

        Log.d(TAG, "Returning from onStartCommand.");
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance()
    {
        Camera c = null;
        try
        {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e)
        {
            // Camera is not available (in use or does not exist)
            Log.e(TAG, "Could not get camera instance: " + e);
        }

        return c; // returns null if camera is unavailable
    }

    @Override
    public void onBackPressed()
    {
        Log.d(TAG, "onBackPressed start");
        super.onBackPressed();
        Log.d(TAG, "onBackPressed done");
    }

    @Override
    public boolean onTouch(View v, MotionEvent evt)
    {
        return false;
    }

    private boolean isServiceRunning(String serviceName)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceName.equals(service.service.getClassName()))
            {
                return true;
            }
        }

        return false;
    }

    /** The connection to the camera capture service.
     *
     * This ServiceConnection is responsible for the connection between this
     * activity and the camera capture (CameraService) service.
     */
    ServiceConnection mCameraServiceConnection = new ServiceConnection()
    {
        public void onServiceDisconnected(ComponentName name)
        {
            Toast.makeText(mainActivity, "Service is disconnected", 1000).show();
            mBoundService = null;
            /*Toast.makeText(Client.this, "Service is disconnected", 1000).show();
            mBounded = false;
            mServer = null;*/
        }

        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.d(TAG, "Service bounded.");
            Toast.makeText(mainActivity, "Service is connected", 1000).show();

            mBoundService = ((CameraService.LocalBinder)service).getService();
            mBoundService.setCameraInstance(camera);
            /*Toast.makeText(Client.this, "Service is connected", 1000).show();
            mBounded = true;
            LocalBinder mLocalBinder = (LocalBinder)service;
            mServer = mLocalBinder.getServerInstance();*/
            Log.d(TAG, "Service bounded done.");
        }
    };
}
