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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
    Button clearStylesButton;
    Spinner styleSelectorSpinner;

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

        clearStylesButton = findViewById(R.id.clearStyles);
        clearStylesButton.setOnClickListener(clearStylesButtonOnClickListener);

        styleSelectorSpinner = findViewById(R.id.styleSelectorSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dbManager.getAllPaintersNames());
        styleSelectorSpinner.setAdapter(adapter);
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
            Log.w(loggerTag, "Permission not granted!");
            requestPermissions(new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 1);
        }
        startGalleryActivityForResult();
    };  // chooseStylePhotoOnClickListener

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
            Log.e(loggerTag, "Error! " + e.toString());
        }

        loadedPhoto = BitmapFactory.decodeStream(imageStream);
    }

    /**
     * Handles operations onActivityResult for Activities started from within this Activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMG && resultCode == Activity.RESULT_OK) {
            onGalleryResult(data);
        }
    }

    /**
     * Saves style to DB using dbManager
     *
     * @param painterName given painter name
     * @param stylePhoto loaded style image
     */
    final void saveStyle(String painterName, Bitmap stylePhoto) {
        if(dbManager.getAllPaintersNames().contains(painterName)) {  // check if name is already in DB
            Toast.makeText(context, "Name already used!", Toast.LENGTH_LONG).show();
        } else {
            try {
                String stylePhotoPath = Utils.savePhotoToFile(stylePhoto, context);
                dbManager.addStyle(painterName, stylePhotoPath);
                Toast.makeText(context, "Style added!", Toast.LENGTH_LONG).show();
            } catch(Exception e) {
                Log.e(loggerTag, "Error! " + e.toString());
            }
        }
    }  // addStyle

    /**
     * Handles operations when clicking on saveStyleButton
     */
    final View.OnClickListener saveStyleButtonOnClickListener = v -> {
        String painterName = painterNameEditText.getText().toString();

        if (!painterName.equals("") && loadedPhoto != null) {
            saveStyle(painterName, loadedPhoto);
        } else {
            Toast.makeText(context, "Set style name and load photo!", Toast.LENGTH_LONG).show();
        }
    };  // saveStyleButtonOnClickListener


    /**
     * Handles operations when clicking on clearStylesButton
     */
    final View.OnClickListener clearStylesButtonOnClickListener = v -> {
        dbManager.clearRecord(styleSelectorSpinner.getSelectedItem().toString());
        Toast.makeText(context, "Style Cleared!", Toast.LENGTH_LONG).show();
    };  // clearStylesButtonOnClickListener
}