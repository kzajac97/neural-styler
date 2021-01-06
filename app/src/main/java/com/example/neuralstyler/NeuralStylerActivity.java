package com.example.neuralstyler;

import android.Manifest;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.example.neuralstyler.ml.MagentaArbitraryImageStylizationV1256Int8Prediction1;

import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;


@RequiresApi(api = Build.VERSION_CODES.M)
public class NeuralStylerActivity extends AppCompatActivity {
    ImageView inputImageView;
    Button savePhotoButton;
    Button stylizePhotoButton;
    Spinner styleSelectorSpinner;

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

        inputImageView = findViewById(R.id.inputImageView);

        savePhotoButton = findViewById(R.id.savePhotoButton);
        savePhotoButton.setOnClickListener(savePhotoButtonOnClickListener);

        stylizePhotoButton = findViewById(R.id.stylizePhotoButton);
        stylizePhotoButton.setOnClickListener(stylizePhotoButtonOnClickListener);

        styleSelectorSpinner = findViewById(R.id.styleSelectorSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dbManager.getAllPaintersNames());
        styleSelectorSpinner.setAdapter(adapter);

        Intent startedWithIntent = getIntent();
        if (startedWithIntent.hasExtra("image")) {  // when starting from MainActivity
            try {
                FileInputStream inputStream = this.openFileInput(startedWithIntent.getStringExtra("image"));
                Bitmap inputImage = BitmapFactory.decodeStream(inputStream);
                inputImageView.setImageBitmap(inputImage);
            } catch (FileNotFoundException e) {
                Log.e(loggerTag, e.toString());
            }
        }

        else {  // when starting from Gallery or Camera
            Log.d(loggerTag, startedWithIntent.getDataString());
            try {
                FileInputStream inputStream = (FileInputStream) context.getContentResolver().openInputStream(Uri.parse(startedWithIntent.getDataString()));
                Bitmap inputImage = BitmapFactory.decodeStream(inputStream);
                inputImageView.setImageBitmap(inputImage);
            } catch (Exception e) {
                Log.e(loggerTag, e.toString());
            }
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
     * Saves generated image into Gallery
     */
    final View.OnClickListener savePhotoButtonOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.w(loggerTag, "Permission not granted!");
            requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }
        saveImageToGallery(Utils.getBitmapFromImageView(inputImageView));
    };  // savePhotoButtonOnClickListener

    /**
     * Saves image to MediaStore
     *
     * @param image Bitmap object with generated image
     */
    final void saveImageToGallery(Bitmap image) {
        MediaStore.Images.Media.insertImage(
            getContentResolver(),
            image,
            "NeuralStyleImage",
            "Image Generated with Neural Style Transfer algorithm."
        );

        Toast.makeText(context, "Image saved!", Toast.LENGTH_LONG).show();
    }

    /**
     *
     */
    final View.OnClickListener stylizePhotoButtonOnClickListener = v -> {
        // TODO: TFLite model launched here
        // Bitmap bitmap = dbManager.getImageForPainter("Item");
        Bitmap photo = Utils.getBitmapFromImageView(inputImageView);

        try {
            Log.d(loggerTag, "Starting Stylizer!");
            Toast.makeText(context, "Running Model!", Toast.LENGTH_LONG).show();
            Log.d(loggerTag, "Building model...");
            MagentaArbitraryImageStylizationV1256Int8Prediction1 model = MagentaArbitraryImageStylizationV1256Int8Prediction1.newInstance(context);
            Log.d(loggerTag, "Converting image to tensor...");
            TensorImage styleImage = TensorImage.fromBitmap(photo);
            Log.d(loggerTag, "Running model...");
            MagentaArbitraryImageStylizationV1256Int8Prediction1.Outputs outputs = model.process(styleImage);
            Log.d(loggerTag, "Getting outputs...");
            TensorBuffer styleBottleneck = outputs.getStyleBottleneckAsTensorBuffer();
            Log.d(loggerTag, "Converting result to ByteBuffer");
            ByteBuffer buffer = styleBottleneck.getBuffer();
            Log.d(loggerTag, "Buffer Shape" + Arrays.toString(styleBottleneck.getShape()));
            Log.d(loggerTag, "Converting to bitmap...");

            Log.d(loggerTag, "Setting to ImageView");
            Toast.makeText(context, "Image Stylized!", Toast.LENGTH_LONG).show();
            // inputImageView.setImageBitmap(bitmap);
            // TODO: Set output to ImageView

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            Log.e(loggerTag, "Error!" + e.toString());
        }
    };  // stylizePhotoButtonOnClickListener
}