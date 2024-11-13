package com.example.cwk_mwe.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.cwk_mwe.utils.AudiobookPlayer;

public class AudioPlayerService extends Service {
    public static final String ACTION_LOAD = "com.example.cwk_mwe.ACTION_LOAD";
    public static final String ACTION_PLAY = "com.example.cwk_mwe.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.cwk_mwe.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.cwk_mwe.ACTION_STOP";

    private AudiobookPlayer audiobookPlayer;

    @Override
    public void onCreate() {
        super.onCreate();
        audiobookPlayer = new AudiobookPlayer();
        Log.d("AudioPlayerService", "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String path = intent.getStringExtra("path");
        if (action != null) {
            switch (action) {
                case ACTION_LOAD:
                    if (path != null) {
                        audiobookPlayer.load(path, 1); // Load the specified path
                    }
                    break;
                case ACTION_PLAY:
                    audiobookPlayer.play();
                    startNotificationService(NotificationService.ACTION_SHOW_NOTIFICATION);
                    break;
                case ACTION_PAUSE:
                    audiobookPlayer.pause();
                    break;
                case ACTION_STOP:
                    audiobookPlayer.stop();
                    startNotificationService(NotificationService.ACTION_HIDE_NOTIFICATION);
                    stopSelf();
                    return START_NOT_STICKY;
            }
        } else {
            Log.d("AudioPlayerService", "Null action");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audiobookPlayer != null) {
            audiobookPlayer.stop();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startNotificationService(String action) {
        Intent notificationIntent = new Intent(this, NotificationService.class);
        notificationIntent.setAction(action);
        startService(notificationIntent);
    }
}