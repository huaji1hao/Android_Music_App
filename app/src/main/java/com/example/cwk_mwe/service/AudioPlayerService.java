package com.example.cwk_mwe.service;

import static com.example.cwk_mwe.utils.AppUtils.ACTION_LOAD;
import static com.example.cwk_mwe.utils.AppUtils.ACTION_STOP;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cwk_mwe.utils.CallManager;
import com.example.cwk_mwe.utils.EnhancedAudiobookPlayer;
import com.example.cwk_mwe.utils.AppUtils;
import com.example.cwk_mwe.utils.AudiobookPlayer;
import com.example.cwk_mwe.utils.MusicCard;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AudioPlayerService extends Service {
    private final IBinder binder = new LocalBinder();
    private Handler handler;
    private CallManager callManager;
    private EnhancedAudiobookPlayer audiobookPlayer;
    private ArrayList<MusicCard> musicList;
    private int currentIndex = -1;
    private float playbackSpeed = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        audiobookPlayer = new EnhancedAudiobookPlayer();
        playbackSpeed = loadPlaybackSpeed();
        audiobookPlayer.setOnCompletionListener(this::playNext);
        Log.d("AudioPlayerService", "Service created");

        registerCallManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audiobookPlayer != null) {
            audiobookPlayer.stop();
        }
        Log.d("AudioPlayerService", "Service destroyed");

        if(callManager != null) {
            callManager.unregister();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_LOAD:
                    handleLoadAction(intent);
                    break;
                case ACTION_STOP:
                    handleStopAction();
                    return START_NOT_STICKY;
                default:
                    Log.d("AudioPlayerService", "Unsupported action: " + action);
            }
        } else {
            Log.d("AudioPlayerService", "Null action");
        }
        return START_STICKY;
    }

    private void handleLoadAction(Intent intent) {
        Serializable serializableList = intent.getSerializableExtra("musicList");
        if (serializableList instanceof ArrayList<?>) {
            musicList = (ArrayList<MusicCard>) serializableList;
            Log.d("AudioPlayerService", "Music list loaded with " + musicList.size() + " items");
        }

        String path = intent.getStringExtra("path");
        if (path != null) {
            File file = new File(path);
            if (!file.exists()) {
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(() -> Toast.makeText(this, "Music file does not exist", Toast.LENGTH_SHORT).show());
                return;
            }

            currentIndex = getCurrentIndex(path);
            audiobookPlayer.load(path, playbackSpeed); // Load the specified path
            manageNotificationService(NotificationService.ACTION_SHOW_NOTIFICATION);
        }

        int progress = intent.getIntExtra("progress", 0);
        audiobookPlayer.skipTo(progress); // Skip to the specified progress
    }

    private void handleStopAction() {
        audiobookPlayer.stop();
        manageNotificationService(NotificationService.ACTION_HIDE_NOTIFICATION);
        stopSelf();
    }

    public void manageNotificationService(String action) {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.setAction(action);
        startService(notificationIntent);
    }

    public MusicCard getCurrentMusicInfo() {
        if (currentIndex >= 0 && currentIndex < musicList.size()) {
            return musicList.get(currentIndex);
        }
        return null;
    }

    private int getCurrentIndex(String path) {
        for (int i = 0; i < musicList.size(); i++) {
            if (musicList.get(i).path.equals(path)) {
                return i;
            }
        }
        return -1;
    }

    public int getDuration() {
        if(currentIndex < 0 || currentIndex >= musicList.size())
            return 0;
        return Integer.parseInt(musicList.get(currentIndex).duration);
    }

    public int getCurrentProgress() {
        return audiobookPlayer.getProgress();
    }

    public void seekTo(int progress) {
        audiobookPlayer.skipTo(progress);
    }

    public void play() {
        audiobookPlayer.play();
    }

    public void pause() {
        audiobookPlayer.pause();
    }

    public void playNext() {
        if (musicList != null && !musicList.isEmpty()) {
            currentIndex = (currentIndex + 1) % musicList.size();
            playCurrent();
        }
    }

    public void playPrev() {
        if (musicList != null && !musicList.isEmpty()) {
            currentIndex = (currentIndex - 1 + musicList.size()) % musicList.size();
            playCurrent();
        }
    }

    private void playCurrent() {
        if (currentIndex >= 0 && currentIndex < musicList.size()) {
            audiobookPlayer.stop();
            String path = musicList.get(currentIndex).path;
            audiobookPlayer.load(path, playbackSpeed);
            audiobookPlayer.play();
            manageNotificationService(NotificationService.ACTION_SHOW_NOTIFICATION);
            if (handler != null){
                handler.sendEmptyMessage(AppUtils.MSG_UPDATE_MUSIC_INFO);
            }
        }
    }

    public boolean isPlaying() {
        return audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING;
    }

    public void setPlaybackSpeed(float speed) {
        playbackSpeed = speed;
        audiobookPlayer.setPlaybackSpeed(speed);
        savePlaybackSpeed(speed);
    }

    public float getPlaybackSpeed() {
        return playbackSpeed;
    }

    private void savePlaybackSpeed(float speed) {
        SharedPreferences sharedPreferences = getSharedPreferences("AudioPlayerPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("playbackSpeed", speed);
        editor.apply();
    }

    private float loadPlaybackSpeed() {
        SharedPreferences sharedPreferences = getSharedPreferences("AudioPlayerPrefs", MODE_PRIVATE);
        return sharedPreferences.getFloat("playbackSpeed", 1.0f); // Default speed is 1.0f
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    private void registerCallManager() {
        Executor executor = Executors.newSingleThreadExecutor();
        callManager = new CallManager(this, this, executor);
        callManager.register();
    }
}