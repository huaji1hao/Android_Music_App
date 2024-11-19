package com.example.cwk_mwe.activity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_BACKGROUND_COLOR = "background_color";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyBackgroundColor();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyBackgroundColor();
    }

    // Load the background color from SharedPreferences and apply it to the activity
    protected void applyBackgroundColor() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int defaultColor = Color.parseColor("#CFC7F8");
        int color = prefs.getInt(KEY_BACKGROUND_COLOR, defaultColor);
        getWindow().getDecorView().setBackgroundColor(color);
    }

    // Save the background color to SharedPreferences and apply it to the activity
    protected void applyBackgroundColor(int color) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_BACKGROUND_COLOR, color);
        editor.apply();
        applyBackgroundColor();
    }

}