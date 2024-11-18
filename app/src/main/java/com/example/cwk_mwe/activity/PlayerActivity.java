package com.example.cwk_mwe.activity;

import static com.example.cwk_mwe.utils.AppUtils.MSG_UPDATE_MUSIC_INFO;
import static com.example.cwk_mwe.utils.AppUtils.formatTime;
import static com.example.cwk_mwe.utils.AppUtils.musicCardToJson;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.RequiresApi;

import com.example.cwk_mwe.R;
import com.example.cwk_mwe.service.AudioPlayerService;
import com.example.cwk_mwe.service.NotificationService;
import com.example.cwk_mwe.utils.MusicCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PlayerActivity extends BaseActivity {
    private ImageView playPauseButton;
    private AudioPlayerService audioPlayerService;
    private boolean isBound = false;
    private TextView musicTitleTextView;
    private TextView musicArtistTextView;
    private TextView musicAlbumTextView;
    private SeekBar seekBar;
    private Handler seekbarHandler = new Handler();

    private Handler musicInfoHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_UPDATE_MUSIC_INFO) {
                updateMusicInfo();
                updateCassetteRotation();
            }
        }
    };
    private ObjectAnimator rotationAnimator;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioPlayerService = binder.getService();
            isBound = true;
            audioPlayerService.setHandler(musicInfoHandler);
            updateMusicInfo();
            updatePlayPauseButton();
            updateSeekBar();
            updateCassetteRotation();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            stopSeekBarUpdate();
            stopCassetteRotation();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_player);

        setupUI();
        setupPlaybackSpeed();
        setupSeekBar();
        setupBookmarkIcon();
    }

    private void setupUI() {
        ImageButton backButton = findViewById(R.id.back_button0);
        backButton.setOnClickListener(v -> finish());

        playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setOnClickListener(v -> {
            if (isBound) {
                MusicCard currentMusic = audioPlayerService.getCurrentMusicInfo();
                if (currentMusic == null) {
                    Toast.makeText(this, "Failed to play music: \nNo current music", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (audioPlayerService.isPlaying()) {
                    audioPlayerService.pause();
                    audioPlayerService.manageNotificationService(NotificationService.ACTION_HIDE_NOTIFICATION);
                    playPauseButton.setImageResource(R.drawable.ic_play);
                    pauseCassetteRotation();
                } else {
                    audioPlayerService.play();
                    audioPlayerService.manageNotificationService(NotificationService.ACTION_SHOW_NOTIFICATION);
                    playPauseButton.setImageResource(R.drawable.ic_pause);
                    resumeCassetteRotation();
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

        musicTitleTextView = findViewById(R.id.music_title);
        musicArtistTextView = findViewById(R.id.music_artist);
        musicAlbumTextView = findViewById(R.id.music_album);

        ImageView stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(v -> {
            if (isBound) {
                audioPlayerService.seekTo(0);
                audioPlayerService.pause();
                audioPlayerService.manageNotificationService(NotificationService.ACTION_HIDE_NOTIFICATION);
                seekBar.setProgress(0);
                updateMusicTime();
                updatePlayPauseButton();
                pauseCassetteRotation();
            }
        });
    }

    private void setupPlaybackSpeed() {
        TextView playbackSpeedTextView = findViewById(R.id.playback_speed);

        // Load playback speed from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("AudioPlayerPrefs", MODE_PRIVATE);
        float playbackSpeed = sharedPreferences.getFloat("playbackSpeed", 1.0f); // Default speed is 1.0f
        playbackSpeedTextView.setText(String.format("x%.1f", playbackSpeed));
    }

    private void setupSeekBar() {
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

    private void setupBookmarkIcon() {
        ImageView bookmarkIcon = findViewById(R.id.bookmark_icon);
        bookmarkIcon.setOnClickListener(v -> {
            if (isBound) {
                MusicCard currentMusic = audioPlayerService.getCurrentMusicInfo();
                if (currentMusic == null) {
                    Toast.makeText(this, "Failed to add bookmark: \nNo current music", Toast.LENGTH_SHORT).show();
                    return;
                }
                currentMusic.progress = audioPlayerService.getCurrentProgress();

                if (currentMusic != null) {
                    Log.d("PlayerActivity", "Bookmarking music: " + currentMusic.title + " at " + currentMusic.progress);
                    SharedPreferences sharedPreferences = getSharedPreferences("BookmarkPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    // Retrieve existing array of music cards
                    String musicCardsJson = sharedPreferences.getString("musicCards", "[]");
                    JSONArray musicCardsArray;
                    try {
                        musicCardsArray = new JSONArray(musicCardsJson);
                    } catch (JSONException e) {
                        musicCardsArray = new JSONArray();
                    }

                    // Add new music card and progress
                    JSONObject musicCardObject = musicCardToJson(currentMusic);
                    musicCardsArray.put(musicCardObject);

                    // Save updated array back to SharedPreferences
                    editor.putString("musicCards", musicCardsArray.toString());
                    editor.apply();

                    Toast.makeText(this, "Bookmark added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add bookmark", Toast.LENGTH_SHORT).show();
                }
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

            seekbarHandler.postDelayed(this::updateSeekBar, 1000);
        }
    }

    private void stopSeekBarUpdate() {
        seekbarHandler.removeCallbacksAndMessages(null);
    }

    private void updateCassetteRotation() {
        if (!audioPlayerService.isPlaying()) return;
        if (rotationAnimator == null) {
            ImageView cassetteImage = findViewById(R.id.cassette_image);
            rotationAnimator = ObjectAnimator.ofFloat(cassetteImage, "rotation", 0f, 360f);
            rotationAnimator.setDuration(10000); // 10 seconds for one full rotation
            rotationAnimator.setInterpolator(new LinearInterpolator());
            rotationAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        }
        rotationAnimator.start();
    }

    private void pauseCassetteRotation() {
        if (rotationAnimator != null && rotationAnimator.isRunning()) {
            rotationAnimator.pause();
        }
    }

    private void resumeCassetteRotation() {
        if (rotationAnimator != null && rotationAnimator.isPaused()) {
            rotationAnimator.resume();
        }
    }

    private void stopCassetteRotation() {
        if (rotationAnimator != null) {
            rotationAnimator.end();
        }
    }
}