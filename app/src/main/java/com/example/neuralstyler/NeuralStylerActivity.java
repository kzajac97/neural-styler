package com.example.neuralstyler;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NeuralStylerActivity extends AppCompatActivity {
    // UI controls
    ImageView inputImageView;
    Button savePhotoButton;
    Button stylizePhotoButton;
    Spinner styleSelectorSpinner;
    // private variables
    private final String loggerTag = "NeuralStylerLogger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neural_styler);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inputImageView = findViewById(R.id.inputImageView);

        savePhotoButton = findViewById(R.id.savePhotoButton);
        savePhotoButton.setOnClickListener(savePhotoButtonOnClickListener);

        stylizePhotoButton = findViewById(R.id.stylizePhotoButton);
        stylizePhotoButton.setOnClickListener(stylizePhotoButtonOnClickListener);

        styleSelectorSpinner = findViewById(R.id.styleSelectorSpinner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == R.id.action_settings) {
            Log.d(loggerTag, "Entering settings");
            Intent enterSettingIntent = new Intent(this, SettingsActivity.class);
            startActivity(enterSettingIntent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Saves generated image into Gallery
     */
    final View.OnClickListener savePhotoButtonOnClickListener = v -> {
        // TODO: Save saving image here
    };  // savePhotoButtonOnClickListener

    /**
     *
     */
    final View.OnClickListener stylizePhotoButtonOnClickListener = v -> {
        // TODO: Save saving image here
    };  // stylizePhotoButtonOnClickListener
}