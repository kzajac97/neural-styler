package com.example.neuralstyler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class Utils {
    final static String loggerTag = "UtilsLogger";  // static because Utils class should not be instantiated
    /**
     * Compresses bitmap to be saved into DB
     *
     * @param photo Bitmap with photo content
     *
     * @return bytes array with compressed photo
     */
    public static byte[] compressBitmap(Bitmap photo) {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, byteOutputStream);

        return byteOutputStream.toByteArray();
    }

    /**
     * Decompresses Bitmap loaded from DB as byte array
     *
     * @param compressed compressed Bitmap
     *
     * @return decompressed displayable Bitmap
     */
    public static Bitmap decompressBitmap(byte[] compressed) {
        return BitmapFactory.decodeByteArray(compressed, 0, compressed.length);
    }

    /**
     * Get Bitmap object from ImageView anywhere across app
     *
     * @param view non-empty ImageView object
     *
     * @return Bitmap stored in image view converted to android.graphics.Bitmap object
     */
    public static Bitmap getBitmapFromImageView(ImageView view) {
        return ((BitmapDrawable) view.getDrawable()).getBitmap();
    }

    /**
     * Saves image in temporary file to allow passing it easily between intents
     *
     * @param photo Bitmap with photo content
     * @param context application Context, required to determine file location
     *
     * @return File name image was saved to
     */
    public static String savePhotoToFile(Bitmap photo, Context context) {
        final String fileName = "extrasBitmap.png";
        try {
            FileOutputStream fileStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            photo.compress(Bitmap.CompressFormat.PNG, 100, fileStream);
            fileStream.close();  // clean-up
        } catch (IOException e) {
            Log.e(loggerTag, "Error!" + e.toString());
        }

        return fileName;
    }

    /**
     * Load image from given file and context
     *
     * @param bitmapPath path to saved bitmap image
     * @param context application Context, required to determine file location
     *
     * @return loaded Bitmap
     */
    public static Bitmap loadPhotoFromFile(String bitmapPath, Context context) {
        FileInputStream inputStream = null;
        try {
            inputStream = context.openFileInput(bitmapPath);
        } catch (IOException e) {
            Log.e(loggerTag, "Error! " + e.toString());
        }

        return BitmapFactory.decodeStream(inputStream);
    }
}
