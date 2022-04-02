package org.olimar.klog;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;


/** Takes care of the SQLite database for this application.
 */
public class SQLiteHelper extends SQLiteOpenHelper
{
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "photos";
    public static final String COLUMN_PHOTO = "photo";
    public static final String COLUMN_WHEN = "when";
    public static final String TABLE_PHOTOS = "photos";
    private static final String PHOTO_TABLE_CREATE =
        "CREATE TABLE " + TABLE_PHOTOS + " (" +
        COLUMN_PHOTO + " BINARY, " +
        COLUMN_WHEN + " TEXT);";

    SQLiteHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(PHOTO_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int a, int b)
    {
        //db.execSQL(PHOTO_TABLE_CREATE);
    }
}
