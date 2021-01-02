package com.example.neuralstyler;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            Log.w(loggerTag, "SQL DB version do not match");
            // TODO: Fix
            // db.execSQL("DROP TABLE IF EXISTS " + TABLE_POSTS);
            // db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
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

    public void addStyle(String painterName, Bitmap sampleImage) {
        Log.d(loggerTag, "Adding style to DB");

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();  // wrap operations with transaction
        try {
            ContentValues values = new ContentValues();

            values.put(KEY_PAINTER_NAME, painterName);
            values.put(KEY_SAMPLE_IMAGE, compressBitmap(sampleImage));
            Log.d(loggerTag, "Putting data to DB: " + painterName + Arrays.toString(compressBitmap(sampleImage)));

            db.insertOrThrow(TABLE_STYLES, null, values);
            db.setTransactionSuccessful();
        } catch(Exception e) {
            Log.e(loggerTag, "Error!" + e.toString());
        } finally {
            db.endTransaction();
            Log.d(loggerTag, "Transaction successful!");
        }
    }

    public List<String> getAllPaintersNames() {
        List<String> painters = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        // TODO: Do not use raw query!
        String dbQuery = "SELECT" + " " + KEY_PAINTER_NAME + " " + "FROM" + " " + TABLE_STYLES;
        Log.d(loggerTag, "Querying with: " + dbQuery);

        Cursor cursor = db.rawQuery(dbQuery, null);

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

    public Bitmap getImageForPainter(String painterName) {
        SQLiteDatabase db = getReadableDatabase();
        byte[] queryResult = null;

        Log.d(loggerTag, "Running query");
        // Cursor cursor = db.query(TABLE_STYLES, new String[]{KEY_SAMPLE_IMAGE}, KEY_SAMPLE_IMAGE + " = " + "'" + painterName + "'", null, null, null, null);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STYLES, null);
        Log.d(loggerTag, "Query successful");

        try {
            if(cursor.moveToFirst()) {
                do {  // add all queried records to DB
                    String result = cursor.getString(cursor.getColumnIndex(KEY_PAINTER_NAME));
                    Log.d(loggerTag, result);
                    queryResult = cursor.getBlob(cursor.getColumnIndex(KEY_SAMPLE_IMAGE));
                    Log.d(loggerTag, Arrays.toString(queryResult));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(loggerTag, "DB Error! " + e.toString());
        } finally {
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }

        return BitmapFactory.decodeByteArray(queryResult, 0, queryResult.length);
    }

    private byte[] compressBitmap(Bitmap photo) {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, byteOutputStream);
        return byteOutputStream.toByteArray();
    }
}
