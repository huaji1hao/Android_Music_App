package com.example.cwk_mwe.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.cwk_mwe.utils.AudiobookPlayer;
import com.example.cwk_mwe.utils.MusicCard;

import java.io.Serializable;
import java.util.ArrayList;

public class AudioPlayerService extends Service {
    public static final String ACTION_LOAD = "com.example.cwk_mwe.ACTION_LOAD";
    public static final String ACTION_STOP = "com.example.cwk_mwe.ACTION_STOP";
    private final IBinder binder = new LocalBinder();

    private AudiobookPlayer audiobookPlayer;
    private ArrayList<MusicCard> musicList;
    private int currentIndex = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        audiobookPlayer = new AudiobookPlayer();
        Log.d("AudioPlayerService", "Service created");
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
        String action = intent.getAction();
        if (action != null) {
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
            currentIndex = getCurrentIndex(path);
            audiobookPlayer.load(path, 1); // Load the specified path
            startNotificationService(NotificationService.ACTION_SHOW_NOTIFICATION);
        }
    }

    private void handleStopAction() {
        audiobookPlayer.stop();
        startNotificationService(NotificationService.ACTION_HIDE_NOTIFICATION);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audiobookPlayer != null) {
            audiobookPlayer.stop();
        }
    }

    private void startNotificationService(String action) {
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
        startNotificationService(NotificationService.ACTION_SHOW_NOTIFICATION);
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
            audiobookPlayer.load(path, 1);
            audiobookPlayer.play();
            startNotificationService(NotificationService.ACTION_SHOW_NOTIFICATION);
        }
    }

    public boolean isPlaying() {
        return audiobookPlayer.getState() == AudiobookPlayer.AudiobookPlayerState.PLAYING;
    }
}