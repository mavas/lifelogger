package org.olimar.klog;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


/** Takes care of storing picture data after picture taken.
 *
 * Handles data either via directory files or SQLite database.
 *
 * @author David Kilgore
 */
public class PhotoHandler implements PictureCallback
{
    private final Context context;
    private final static String TAG = "KLOG";

    public PhotoHandler(Context context)
    {
        this.context = context;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {
        Log.d(TAG, "Starting to insert photo.");
        sqliteHandle(data, camera);
    }

    private void sqliteHandle(byte[] data, Camera camera)
    {
        SQLiteDatabase db = (new SQLiteHelper(this.context)).getWritableDatabase();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyymmddhhmmss");
        String date = dateFormat.format(new Date());

        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_PHOTO, data);
        values.put(SQLiteHelper.COLUMN_WHEN, date);
        long insertID = db.insert(SQLiteHelper.TABLE_PHOTOS, null, values);
        Log.d(TAG, "Done inserting photo.");
    }
}
