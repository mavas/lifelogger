package org.olimar.klog;

import android.app.Service;
import android.os.IBinder;
import android.content.Intent;


/** The upload service,
 *
 * This Android service transfers camera capture data to a remote server.
 *
 * @author David Kilgore
 */
public class UploadService extends Service
{
    @Override
    public int onStartCommand(Intent intent, int flags, int startID)
    {
        loop();
        return START_NOT_STICKY;
    }

    /** Performs the data upload loop.
     *
     * This method implements the loop that forever continuously reads camera
     * capture data from the database and uploads it to the server.
     */
    private void loop()
    {
        // See if there are any photos to up load.
        String query = "SELECT count(*) from photos";
        //Cursor cursor = da
    }

    @Override
    public IBinder onBind(Intent i)
    {
        return null;
    }
}
