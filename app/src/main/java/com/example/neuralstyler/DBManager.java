package com.example.neuralstyler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class DBManager extends SQLiteOpenHelper {
    // db info
    private static final String DB_NAME = "styles_database";
    private static final int DB_VERSION = 1;
    // table names
    private static final String TABLE_STYLES = "styles";
    // column names
    private static final String ID = "id";
    private static final String KEY_PAINTER_NAME = "painter_name";
    private static final String KEY_SAMPLE_IMAGE = "sample_image";
    // private variables
    private final String loggerTag = "DBLogger";
    private static DBManager selfInstance;

    /**
     * Use the application context, which will ensure that you
     * don't accidentally leak an Activity's context.
     *
     * @param context application context in Activity
     *
     * @return instance of DBManager class
     */
    public static synchronized DBManager getInstance(Context context) {
        if (selfInstance == null) {
            selfInstance = new DBManager(context.getApplicationContext());
        }

        return selfInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * method "getInstance()" should be used instead.
     *
     * @param context application context in Activity
     */
    private DBManager(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_STYLES +
                "(" +
                    ID +  " INTEGER PRIMARY KEY,  " +
                    KEY_PAINTER_NAME + " TEXT," +
                    KEY_SAMPLE_IMAGE + " BLOB" +
                ")";

        db.execSQL(CREATE_TABLE);
    }

    /**
     * Called when DB with this name already exists, but has different version
     * Drops current tables and recreates them
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            Log.w(loggerTag, "SQL DB version do not match");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STYLES);
            onCreate(db);
        }
    }

    /**
     * Called when the database connection is being configured.
     * Configure database settings for things like foreign key support, write-ahead logging, etc.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    /**
     * Packs input data into ContentValues object
     *
     * @param painterName unique name for given painter
     * @param sampleImage style photo used by styles across application
     *
     * @return data packed into ContentValues object
     */
    private ContentValues packValues(String painterName, Bitmap sampleImage) {
        ContentValues values = new ContentValues();

        values.put(KEY_PAINTER_NAME, painterName);
        values.put(KEY_SAMPLE_IMAGE, Utils.compressBitmap(sampleImage));

        return values;
    }

    /**
     * Add new artistic style to DB
     *
     * @param painterName unique name for given painter
     * @param sampleImage style photo used by styles across application
     */
    public void addStyle(String painterName, Bitmap sampleImage) {
        Log.d(loggerTag, "Adding style to DB");

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();  // wrap operations with transaction

        try {
            db.insertOrThrow(TABLE_STYLES, null, packValues(painterName, sampleImage));
            db.setTransactionSuccessful();
            Log.d(loggerTag, "Transaction successful!");
        } catch(Exception e) {
            Log.e(loggerTag, "Error!" + e.toString());
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Runs Query to DB to retrieve all stored painters
     *
     * @return all painter names on java list
     */
    public List<String> getAllPaintersNames() {
        List<String> painters = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_STYLES, new String[]{ KEY_PAINTER_NAME }, null, null, null, null, null);

        try {
            if(cursor.moveToFirst()) {
                do {  // add all queried records to DB
                    painters.add(cursor.getString(cursor.getColumnIndex(KEY_PAINTER_NAME)));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(loggerTag, "DB Error! " + e.toString());
        } finally {  // clean-up cursor
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return painters;
    }

    /**
     * Runs query to select image corresponding to chosen painter name in DB
     *
     * @param painterName painter name to load image for
     *
     * @return Bitmap containing image loaded and decompressed from DB
     */
    public Bitmap getImageForPainter(String painterName) {
        byte[] queryResult = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_STYLES, new String[]{ KEY_SAMPLE_IMAGE }, KEY_PAINTER_NAME + "=?", new String[]{ painterName }, null, null, null);

        try {
            if(cursor.moveToFirst()) {
                do {  // add all queried records to DB
                    queryResult = cursor.getBlob(cursor.getColumnIndex(KEY_SAMPLE_IMAGE));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(loggerTag, "DB Error! " + e.toString());
        } finally {
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return Utils.decompressBitmap(queryResult);
    }

    /**
     * Clears all records from DB
     * Does not drop the tables, only clears records
     */
    public void clearRecords() {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_STYLES, null, null);
        db.close();
    }
}
