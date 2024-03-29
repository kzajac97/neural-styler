package com.example.neuralstyler;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


@RequiresApi(api = Build.VERSION_CODES.M)
public class NeuralStylerActivity extends AppCompatActivity {
    ImageView mainImageView;
    ImageButton spinnerMockButton;
    ImageButton sharePhotoButton;
    ImageButton savePhotoButton;
    ImageButton stylizePhotoButton;
    Spinner styleSelectorSpinner;
    ProgressBar progressBar;

    private DBManager dbManager;
    private final String loggerTag = "NeuralStylerLogger";
    private Context context;

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
        context = getApplicationContext();

        progressBar = findViewById(R.id.progressBar);
        mainImageView = findViewById(R.id.mainImageView);

        sharePhotoButton = findViewById(R.id.sharePhotoButton);
        sharePhotoButton.setOnClickListener(sharePhotoButtonOnClickListener);

        savePhotoButton = findViewById(R.id.savePhotoButton);
        savePhotoButton.setOnClickListener(savePhotoButtonOnClickListener);

        stylizePhotoButton = findViewById(R.id.stylizePhotoButton);
        stylizePhotoButton.setOnClickListener(stylizePhotoButtonOnClickListener);

        styleSelectorSpinner = findViewById(R.id.styleSelectorSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dbManager.getAllPaintersNames());
        styleSelectorSpinner.setAdapter(adapter);

        spinnerMockButton = findViewById(R.id.mimicSpinnerButton);
        spinnerMockButton.setOnClickListener(v -> styleSelectorSpinner.performClick());

        Bitmap inputImage = BitmapFactory.decodeStream(getImageInputStream(getIntent()));
        mainImageView.setImageBitmap(inputImage);
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
            Intent enterSettingIntent = new Intent(this, SettingsActivity.class);
            startActivity(enterSettingIntent);

            return true;
        }

        if (id == R.id.action_add_style) {  // add style button
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
     * Get FileInputStream with image URI used in activity
     *
     * @param activityStartingIntent intent triggering activity start
     *
     * @return FileStream for image which will be used in Activity
     */
    private FileInputStream getImageInputStream(Intent activityStartingIntent) {
        FileInputStream inputStream = null;

        try {
            if (activityStartingIntent.hasExtra(Const.imageIntentExtraLabel)) {
                // when starting from MainActivity
                inputStream = this.openFileInput(activityStartingIntent.getStringExtra(Const.imageIntentExtraLabel));
            } else {  // when starting from Gallery or Camera Activity
                inputStream = (FileInputStream) context.getContentResolver().openInputStream(Uri.parse(activityStartingIntent.getDataString()));
            }
        } catch (FileNotFoundException e) {
            Log.e(loggerTag, e.toString());
        }

        return inputStream;
    }

    /**
     * Saves generated image into Gallery
     */
    final View.OnClickListener savePhotoButtonOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(loggerTag, "Permission not granted!");
            requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }
        saveImageToGallery(Utils.getBitmapFromImageView(mainImageView));
    };  // savePhotoButtonOnClickListener

    /**
     * shares generated image using Intent
     */
    final View.OnClickListener sharePhotoButtonOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(loggerTag, "Permission not granted!");
            requestPermissions(new String[] { Manifest.permission.SEND_SMS }, 1);
        }
        shareImage(Utils.getBitmapFromImageView(mainImageView));
    };

    /**
     * Saves image to MediaStore
     *
     * @param image Bitmap object with generated image
     */
    final void saveImageToGallery(Bitmap image) {
        MediaStore.Images.Media.insertImage(
            getContentResolver(),
            image,
            Const.neuralStylerImageTitle,
            Const.neuralStylerImageDescription
        );

        Toast.makeText(context, "Image saved!", Toast.LENGTH_LONG).show();
    }

    /**
     * Shares image using Intent
     *
     * @param photo Bitmap object with generated image
     */
    final void shareImage(Bitmap photo) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        String path = Environment.getExternalStorageDirectory() + File.separator + Const.temporaryImageName;
        shareIntent.setType(Const.imageSaveType);

        File f = new File(path);

        try {
            FileOutputStream outputStream = new FileOutputStream(f);
            outputStream.write(Utils.bitmapToStream(photo).toByteArray());
        } catch (IOException e) {
            Log.e(loggerTag, e.toString());
        }

        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(Const.sdcardPath));
        startActivity(Intent.createChooser(shareIntent, Const.neuralStylerImageTitle));
    }

    /**
     * Handles actions on stylizePhotoButton click
     * Creates and runs MagentaModel
     */
    final View.OnClickListener stylizePhotoButtonOnClickListener = v -> {
        String selectedStyle = styleSelectorSpinner.getSelectedItem().toString();
        Bitmap styleImage = Utils.loadPhotoFromFile(dbManager.getImagePathForPainter(selectedStyle), context);
        Bitmap contentImage = Utils.getBitmapFromImageView(mainImageView);

        progressBar.setVisibility(View.VISIBLE);

        MagentaModel model = new MagentaModel(context);
        Bitmap stylizedImage = model.transferStyle(contentImage, styleImage);

        progressBar.setVisibility(View.INVISIBLE);
        Toast.makeText(context, "Image stylized!", Toast.LENGTH_LONG).show();
        mainImageView.setImageBitmap(stylizedImage);
    };  // stylizePhotoButtonOnClickListener
}