package com.example.neuralstyler;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import java.io.FileInputStream;


@RequiresApi(api = Build.VERSION_CODES.M)
public class NeuralStylerActivity extends AppCompatActivity {
    // UI controls
    ImageView inputImageView;
    Button savePhotoButton;
    Button stylizePhotoButton;
    Spinner styleSelectorSpinner;
    // private variables
    private DBManager dbManager;
    private final String loggerTag = "NeuralStylerLogger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neural_styler);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        dbManager = DBManager.getInstance(this);

        inputImageView = findViewById(R.id.inputImageView);

        savePhotoButton = findViewById(R.id.savePhotoButton);
        savePhotoButton.setOnClickListener(savePhotoButtonOnClickListener);

        stylizePhotoButton = findViewById(R.id.stylizePhotoButton);
        stylizePhotoButton.setOnClickListener(stylizePhotoButtonOnClickListener);

        styleSelectorSpinner = findViewById(R.id.styleSelectorSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dbManager.getAllPaintersNames());
        styleSelectorSpinner.setAdapter(adapter);

        try {
            Log.d(loggerTag, "Loading image from Bundle");
            Intent startedWithIntent = getIntent();

            FileInputStream inputStream = this.openFileInput(startedWithIntent.getStringExtra("image"));
            Bitmap inputImage = BitmapFactory.decodeStream(inputStream);

            inputImageView.setImageBitmap(inputImage);
        } catch (Exception e) {
            Log.e(loggerTag, "Error!" + e.toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {  // settings button
            Log.d(loggerTag, "Entering settings");
            Intent enterSettingIntent = new Intent(this, SettingsActivity.class);
            startActivity(enterSettingIntent);

            return true;
        }

        if (id == R.id.action_add_style) {  // add style button
            Log.d(loggerTag, "Entering StyleManagementActivity");
            Intent enterStyleManagementIntent = new Intent(this, StyleManagementActivity.class);
            startActivity(enterStyleManagementIntent);

            return true;
        }

        if (id == R.id.home) {  // Back button
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Saves generated image into Gallery
     */
    final View.OnClickListener savePhotoButtonOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(loggerTag, "Permission not granted!");
            requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }
        saveImageToGallery(((BitmapDrawable) inputImageView.getDrawable()).getBitmap());
    };  // savePhotoButtonOnClickListener

    /**
     * Saves image to MediaStore
     *
     * @param image Bitmap object with generated image
     */
    final void saveImageToGallery(Bitmap image) {
        Log.d(loggerTag, "Saving image:" + image.toString());
        MediaStore.Images.Media.insertImage(
                getContentResolver(),
                image,
                "NeuralStyleImage",
                "Image Generated with Neural Style Transfer algorithm."
        );
    }

    /**
     *
     */
    final View.OnClickListener stylizePhotoButtonOnClickListener = v -> {
        Bitmap b = dbManager.getImageForPainter("Item");
        inputImageView.setImageBitmap(b);
    };  // stylizePhotoButtonOnClickListener
}