package com.example.cwk_mwe.activity;

import static com.example.cwk_mwe.utils.AppUtils.formatTime;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import com.example.cwk_mwe.R;
import com.example.cwk_mwe.service.AudioPlayerService;
import com.example.cwk_mwe.utils.MusicCard;

public class PlayerActivity extends AppCompatActivity {

    private ImageView playPauseButton;
    private AudioPlayerService audioPlayerService;
    private boolean isBound = false;
    private TextView musicTitleTextView;
    private TextView musicArtistTextView;
    private TextView musicAlbumTextView;
    private SeekBar seekBar;
    private Handler handler = new Handler();

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioPlayerService = binder.getService();
            isBound = true;
            updateMusicInfo();
            updatePlayPauseButton();
            updateSeekBar();
            startCassetteRotation();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
//            stopSeekBarUpdate();
            stopCassetteRotation();
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
                updateMusicInfo();
            }
        });

        ImageView prevButton = findViewById(R.id.prev_button);
        prevButton.setOnClickListener(v -> {
            if (isBound) {
                if (!audioPlayerService.isPlaying()) {
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                }
                audioPlayerService.playPrev();
                updateMusicInfo();
            }
        });

        musicTitleTextView = findViewById(R.id.music_title);
        musicArtistTextView = findViewById(R.id.music_artist);
        musicAlbumTextView = findViewById(R.id.music_album);

        seekBar = findViewById(R.id.music_seekbar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && isBound) {
                    audioPlayerService.seekTo(progress);
                    updateMusicTime();
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
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(connection);
            isBound = false;
//            stopSeekBarUpdate();
            stopCassetteRotation();
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

    private void updateMusicInfo() {
        if (isBound) {
            MusicCard music = audioPlayerService.getCurrentMusicInfo();
            if(music != null) {
                musicTitleTextView.setText(music.title);
                musicArtistTextView.setText(music.artist);
                musicAlbumTextView.setText(music.album);
            }
        }
    }

    private void updateMusicTime(){
        TextView elapsedTime = findViewById(R.id.tv_elapsed_time);
        TextView remainingTime = findViewById(R.id.tv_remaining_time);
        int progress = audioPlayerService.getCurrentProgress();

        elapsedTime.setText(formatTime(progress));
        remainingTime.setText(formatTime(audioPlayerService.getDuration() - progress));
    }

    private void updateSeekBar() {
        if (isBound) {
            int duration = audioPlayerService.getDuration();
            int currentPosition = audioPlayerService.getCurrentProgress();
            seekBar.setMax(duration);
            seekBar.setProgress(currentPosition);
            updateMusicTime();

            handler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    private void startCassetteRotation() {
        ImageView cassetteImage = findViewById(R.id.cassette_image);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(cassetteImage, "rotation", 0f, 360f);
        rotation.setDuration(10000); // 10 seconds for one full rotation
        rotation.setInterpolator(new LinearInterpolator());
        rotation.setRepeatCount(ObjectAnimator.INFINITE);
        rotation.start();
    }

    private void stopCassetteRotation() {
        ImageView cassetteImage = findViewById(R.id.cassette_image);
        cassetteImage.clearAnimation();
    }
}