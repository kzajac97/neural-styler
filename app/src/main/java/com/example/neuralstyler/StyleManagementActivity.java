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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import java.io.FileNotFoundException;
import java.io.InputStream;


@RequiresApi(api = Build.VERSION_CODES.M)
public class StyleManagementActivity extends AppCompatActivity {

    EditText painterNameEditText;
    Button chooseStylePhoto;
    Button saveStyleButton;
    // private variables
    Bitmap loadedPhoto = null;
    private DBManager dbManager;
    private static final int RESULT_LOAD_IMG = 2;  // 2 to unify result codes across app
    private final String loggerTag = "NeuralStyleLogger";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_style_management);
        context = getApplicationContext();

        dbManager = DBManager.getInstance(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        painterNameEditText = findViewById(R.id.painterNameEditText);

        chooseStylePhoto = findViewById(R.id.chooseStylePhoto);
        chooseStylePhoto.setOnClickListener(chooseStylePhotoOnClickListener);

        saveStyleButton = findViewById(R.id.saveStyle);
        saveStyleButton.setOnClickListener(saveStyleButtonOnClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.home) {  // Back button
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

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

    final View.OnClickListener chooseStylePhotoOnClickListener = v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.d(loggerTag, "Permission not granted!");
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
        }
        startGalleryActivityForResult();
    };  // chooseStylePhotoOnClickListener

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
            loadedPhoto = BitmapFactory.decodeStream(imageStream);
        }
    }

    final View.OnClickListener saveStyleButtonOnClickListener = v -> {
        if (!painterNameEditText.getText().toString().equals("") && loadedPhoto != null) {
            Log.d(loggerTag, "Saving new style to local database");

            try {
                Log.d(loggerTag, "Saving style with "+ painterNameEditText.getText().toString() + " " + loadedPhoto);
                dbManager.addStyle(painterNameEditText.getText().toString(), loadedPhoto);
            } catch(Exception e) {
                Log.e(loggerTag, "DB Error!" + e.toString());
            }
        } else {
            Log.v(loggerTag, "Name or style photo not set!");
            Toast.makeText(context, "Set style name and load photo!", Toast.LENGTH_LONG).show();
        }
    };  // saveStyleButtonOnClickListener
}