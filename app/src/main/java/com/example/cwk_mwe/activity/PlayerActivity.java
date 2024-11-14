package com.example.cwk_mwe.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cwk_mwe.R;
import com.example.cwk_mwe.service.AudioPlayerService;

public class PlayerActivity extends AppCompatActivity {

    private ImageView playPauseButton;
    private AudioPlayerService audioPlayerService;
    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioPlayerService = binder.getService();
            isBound = true;
            updatePlayPauseButton();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

        ImageButton backButton = findViewById(R.id.back_button0);
        backButton.setOnClickListener(v -> finish());

        playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setOnClickListener(v -> {
            if (isBound) {
                if (audioPlayerService.isPlaying()) {
                    audioPlayerService.pause();
                    playPauseButton.setImageResource(R.drawable.ic_play);
                } else {
                    audioPlayerService.play();
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                }
            }
        });

        ImageView nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(v -> {
            if (isBound) {
                if (!audioPlayerService.isPlaying()) {
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                }
                audioPlayerService.playNext();
            }
        });

        ImageView prevButton = findViewById(R.id.prev_button);
        prevButton.setOnClickListener(v -> {
            if (isBound) {
                if (!audioPlayerService.isPlaying()) {
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                }
                audioPlayerService.playPrev();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudioPlayerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    private void updatePlayPauseButton() {
        if (isBound) {
            boolean isPlaying = audioPlayerService.isPlaying();
            if (isPlaying) {
                playPauseButton.setImageResource(R.drawable.ic_pause);
            } else {
                playPauseButton.setImageResource(R.drawable.ic_play);
            }
        }
    }
}