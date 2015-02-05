package org.olimar.klog;

import java.lang.System;
import java.io.File;
import java.lang.Thread;
import java.util.Date;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.view.SurfaceView;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.hardware.Camera;
import android.os.*;
import android.media.MediaRecorder;
import android.media.CamcorderProfile;
import android.view.SurfaceHolder;
import android.app.Service;
import android.os.IBinder;
import android.content.Intent;
import android.util.Log;

import org.olimar.klog.MainActivity;


/** A service for capturing video and pictures in the background.
 *
 * This service component captures pictures or video and saves them to disk in a
 * SQLite database.
 *
 * @author David Kilgore
 */
public class CameraService extends Service
{
    private static final String TAG = "KLOG";
    private static final int rate_number = 10;
    private static final int rate_cycle = 60;
    final long frameDuration = 1000 / 10;
    private Camera camera = null;
    private boolean running = true;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public class LocalBinder extends Binder
    {
        CameraService getService()
        {
            return CameraService.this;
        }
    }

    public void setCameraInstance(Camera camera)
    {
        this.camera = camera;
    }

    /*@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate();
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startID)
    {
        startCapture();
        return START_STICKY;
    }

    /** Starts capturing via taking many pictures. */
    private int startCapture()
    {
        Log.d(TAG, "Starting camera service.");

        // Wait a second so that the MainActivity can set the camera instance.
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {

        }
        Log.d(TAG, "Done waiting.");

        //camera = MainActivity.getCameraInstance();
        if (camera == null)
        {
            Log.d(TAG, "Camera instance not set yet.");
            return 0;
        }

        final long then = System.currentTimeMillis();
        Log.d(TAG, "Taking picture..");
        try
        {
            camera.takePicture(null, null, new PhotoHandler(getApplicationContext()));
        }
        catch (RuntimeException e)
        {
            Log.d(TAG, "RuntimeException for CameraService: " + e);
            return START_NOT_STICKY;
        }
        Log.d(TAG, "Done.");
        final long now = System.currentTimeMillis();

        final long actualDuration = now - then;
        final long sleepDuration = frameDuration - actualDuration;

        if (sleepDuration > 0)
        {
            try
            {
                Thread.sleep(sleepDuration);
            }
            catch (InterruptedException e)
            {

            }
        }
        else
        {
            Log.d(TAG, "Frame tooo long.");
        }

        Log.d(TAG, "startCapture end");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent i)
    {
        //IBinder mBinder = new LocalBinder();
        return null;
    }

    @Override
    public boolean onUnbind(Intent i)
    {
        //IBinder mBinder = new LocalBinder();
        return true;
    }

    @Override
    public void onDestroy()
    {
        Log.d(TAG, "Stopping camera service.");
        running = false;
        stopSelf();
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
}
