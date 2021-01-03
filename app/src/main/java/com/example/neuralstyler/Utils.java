package com.example.neuralstyler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;


public class Utils {
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
}
