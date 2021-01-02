package com.example.neuralstyler;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.InputStream;


@RequiresApi(api = Build.VERSION_CODES.M)
public class MainActivity extends AppCompatActivity {
    ImageView inputImageView;
    // UI controls
    Button takePhotoButton;
    Button loadPhotoButton;
    Button stylizePhotoButton;
    // private fields
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private final String loggerTag = "NeuralStylerLogger";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        // settings toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // main image display
        inputImageView = findViewById(R.id.inputImageView);
        // load controls
        takePhotoButton = findViewById(R.id.takePhotoButton);
        loadPhotoButton = findViewById(R.id.loadPhotoButton);
        stylizePhotoButton = findViewById(R.id.savePhotoButton);
        // set listeners
        takePhotoButton.setOnClickListener(takePhotoButtonOnClickListener);
        loadPhotoButton.setOnClickListener(loadPhotoButtonOnClickListener);
        stylizePhotoButton.setOnClickListener(stylizePhotoButtonOnClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Handle action bar item clicks here. The action bar will
     * automatically handle clicks on the Home/Up button, so long
     * as you specify a parent activity in AndroidManifest.xml.
     *
     * @param item Menu item clicked-on
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Log.d(loggerTag, "Entering settings");
            Intent enterSettingIntent = new Intent(this, SettingsActivity.class);
            startActivity(enterSettingIntent);

            return true;
        }

        if (id == R.id.action_add_style) {
            Log.d(loggerTag, "Entering StyleManagementActivity");
            Intent enterStyleManagementIntent = new Intent(this, StyleManagementActivity.class);
            startActivity(enterStyleManagementIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Functions handling external camera to take pictures for inside the application
     */
    private void startCameraActivityForResult() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            Log.d(loggerTag, "Starting Camera Activity");
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Log.e(loggerTag, "Error! Activity not found!" + e.toString());
        }
    }

    final View.OnClickListener takePhotoButtonOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.d(loggerTag, "Permission not granted!");
            requestPermissions(new String[] { Manifest.permission.CAMERA }, 1);
        }
        startCameraActivityForResult();
    };  // takePhotoButtonOnClickListener

    /**
     * Functions handling Android Gallery to select any picture
     */
    private void startGalleryActivityForResult() {
        Intent selectPhotoIntent = new Intent(Intent.ACTION_PICK);
        selectPhotoIntent.setType("image/*");

        try {
            Log.d(loggerTag, "Starting Gallery Activity");
            startActivityForResult(selectPhotoIntent, RESULT_LOAD_IMG);
        } catch (ActivityNotFoundException e) {
            Log.e(loggerTag, "Error! Activity not found!" + e.toString());
        }
    }

    final View.OnClickListener loadPhotoButtonOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(loggerTag, "Permission not granted!");
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
        }
        startGalleryActivityForResult();
    };  // loadPhotoButtonOnClickListener

    /**
     * handles actions on activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Loads image from captured with camera
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.d(loggerTag, "Image captured");
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            inputImageView.setImageBitmap(photo);
            Log.d(loggerTag, "Image displayed");
        }

        // Displays image from gallery
        if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            Log.d(loggerTag, "Image loaded");
            // helper variables
            Uri imageUri = data.getData();
            InputStream imageStream = null;
            // try load image from stream
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
            } catch (FileNotFoundException e) {
                Log.e(loggerTag, "Error!" + e.toString());
            }
            // display image on ImageView
            Bitmap photo = BitmapFactory.decodeStream(imageStream);
            inputImageView.setImageBitmap(photo);
            Log.d(loggerTag, "Image displayed");
        }
    }  // onActivityResult

    /**
     * Function starts NeuralStylerActivity
     */
    final View.OnClickListener stylizePhotoButtonOnClickListener = v -> {
        Intent neuralStylerIntent = new Intent(this, NeuralStylerActivity.class);

        if (inputImageView.getDrawable() != null) {
            Log.d(loggerTag, "Putting data into extras Bundle");
            // TODO: Pass via URI, bitmap can crash
            Bitmap image = ((BitmapDrawable) inputImageView.getDrawable()).getBitmap();
            neuralStylerIntent.putExtra("image", image);

            Log.d(loggerTag, "Starting NeuralStyler");
            Log.d(loggerTag, image.toString());
            startActivity(neuralStylerIntent);
        } else {
            Log.d(loggerTag, "Attempted starting NeuralStyler with empty image");
            Toast.makeText(context, "Load Image First!", Toast.LENGTH_LONG).show();
        }
    };  // stylizePhotoButtonOnClickListener

}  // class