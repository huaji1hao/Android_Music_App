package com.example.cwk_mwe.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import com.example.cwk_mwe.R;
import com.example.cwk_mwe.service.AudioPlayerService;

public class SettingActivity extends BaseActivity {
    private AudioPlayerService audioPlayerService;
    private boolean isServiceBound = false;
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioPlayerService = binder.getService();
            isServiceBound = true;

            SeekBar playbackSpeedSeekBar = findViewById(R.id.playback_speed_seekbar);
            TextView currentSpeedTextView = findViewById(R.id.current_speed);
            float initialSpeed = audioPlayerService.getPlaybackSpeed();
            playbackSpeedSeekBar.setProgress((int) ((initialSpeed - 0.1f) * 10));
            currentSpeedTextView.setText(String.format("x%.1f", initialSpeed));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        ImageView changeColorButton1 = findViewById(R.id.change_color_button1);
        ImageView changeColorButton2 = findViewById(R.id.change_color_button2);
        ImageView changeColorButton3 = findViewById(R.id.change_color_button3);

        changeColorButton1.setOnClickListener(v -> applyBackgroundColor(0xFFFAD0C4));
        changeColorButton2.setOnClickListener(v -> applyBackgroundColor(0xFFCFC7F8));
        changeColorButton3.setOnClickListener(v -> applyBackgroundColor(0xffE0C3FC));

        ImageButton backButton = findViewById(R.id.back_button0);
        backButton.setOnClickListener(v -> finish());

        SeekBar playbackSpeedSeekBar = findViewById(R.id.playback_speed_seekbar);
        TextView currentSpeedTextView = findViewById(R.id.current_speed);

        playbackSpeedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isServiceBound) {
                    float speed = 0.1f + (progress / 10.0f);
                    currentSpeedTextView.setText(String.format("x%.1f", speed));
                    audioPlayerService.setPlaybackSpeed(speed);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudioPlayerService.class);
        startService(intent); // Ensure the service is started
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }
    }
}