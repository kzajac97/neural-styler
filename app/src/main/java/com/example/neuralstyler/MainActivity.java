package com.example.neuralstyler;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    Button takePhotoButton;
    Button loadPhotoButton;
    Button stylizePhotoButton;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int RESULT_LOAD_IMG = 2;
    private final String loggerTag = "MainActivityLogger";
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
        inputImageView = findViewById(R.id.mainImageView);
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
            Intent enterSettingIntent = new Intent(this, SettingsActivity.class);
            startActivity(enterSettingIntent);

            return true;
        }

        if (id == R.id.action_add_style) {
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
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Log.e(loggerTag, "Error! Activity not found!" + e.toString());
        }
    }

    /**
     * Handles permissions and starts Camera Activity
     */
    final View.OnClickListener takePhotoButtonOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.w(loggerTag, "Permission not granted!");
            requestPermissions(new String[]{ Manifest.permission.CAMERA }, 1);
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
            startActivityForResult(selectPhotoIntent, RESULT_LOAD_IMG);
        } catch (ActivityNotFoundException e) {
            Log.e(loggerTag, "Error! Activity not found!" + e.toString());
        }
    }

    /**
     * Handles permissions and starts Gallery Activity
     */
    final View.OnClickListener loadPhotoButtonOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(loggerTag, "Permission not granted!");
            requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
        }
        startGalleryActivityForResult();
    };  // loadPhotoButtonOnClickListener

    /**
     * Handles actions on Camera Activity result
     *
     * @param data Camera Intent with captured photo as extras
     */
    final void onCameraResult(Intent data) {
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        inputImageView.setImageBitmap(photo);
    }

    /**
     * Handles actions on Gallery Activity result
     *
     * @param data Gallery Intent with chosen photo as extras
     */
    final void onGalleryResult(Intent data) {
        Uri imageUri = data.getData();
        InputStream imageStream = null;

        try {  // try load image from stream
            imageStream = getContentResolver().openInputStream(imageUri);
        } catch (FileNotFoundException e) {
            Log.e(loggerTag, "Error!" + e.toString());
        }

        Bitmap photo = BitmapFactory.decodeStream(imageStream);
        inputImageView.setImageBitmap(photo);
    }

    /**
     * Handles actions on activity result
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            onCameraResult(data);
        }

        if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            onGalleryResult(data);
        }
    }  // onActivityResult

    /**
     * Function starts NeuralStylerActivity
     */
    final View.OnClickListener stylizePhotoButtonOnClickListener = v -> {
        Intent neuralStylerIntent = new Intent(this, NeuralStylerActivity.class);

        if (inputImageView.getDrawable() != null) {
            Bitmap photo = Utils.getBitmapFromImageView(inputImageView);
            String fileName = Utils.savePhotoToFile(photo, context);
            neuralStylerIntent.putExtra("image", fileName);

            startActivity(neuralStylerIntent);
        } else {
            Toast.makeText(context, "Load Image First!", Toast.LENGTH_LONG).show();
        }
    };  // stylizePhotoButtonOnClickListener
}  // class