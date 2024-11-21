// NotificationService.java
package com.example.cwk_mwe.service;

import static com.example.cwk_mwe.utils.Constants.*;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.cwk_mwe.R;
import com.example.cwk_mwe.models.MusicCard;

public class NotificationService extends Service {

    private AudioPlayerService audioPlayerService;
    private boolean isBound = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioPlayerService.LocalBinder binder = (AudioPlayerService.LocalBinder) service;
            audioPlayerService = binder.getService();
            isBound = true;
            showNotification();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Intent intent = new Intent(this, AudioPlayerService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Audio Player Channel";
            String description = "Channel for audio player notifications";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification() {
        if (audioPlayerService == null) {
            Log.e("NotificationService", "AudioPlayerService is null");
            return;
        }

        MusicCard currentMusic = audioPlayerService.getCurrentMusicInfo();
        if (currentMusic == null) {
            Log.e("NotificationService", "Current music is null");
            return;
        }

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent playPausePendingIntent = PendingIntent.getService(this, 0, new Intent(this, NotificationService.class).setAction(ACTION_PLAY_PAUSE), flags);
        PendingIntent prevPendingIntent = PendingIntent.getService(this, 0, new Intent(this, NotificationService.class).setAction(ACTION_PREV), flags);
        PendingIntent nextPendingIntent = PendingIntent.getService(this, 0, new Intent(this, NotificationService.class).setAction(ACTION_NEXT), flags);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(currentMusic.title)
                .setContentText(currentMusic.artist)
                .setSmallIcon(R.drawable.ic_audiobook)
                .addAction(0, "Previous", prevPendingIntent)
                .addAction(0, "Play/Pause", playPausePendingIntent)
                .addAction(0, "Next", nextPendingIntent)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        String action = intent.getAction();

        if (ACTION_SHOW_NOTIFICATION.equals(action)) {
            if (isBound) {
                showNotification();
            }
        } else if (ACTION_HIDE_NOTIFICATION.equals(action)) {
            stopSelf();
        } else if (isBound) {
            if (ACTION_PLAY_PAUSE.equals(action)) {
                if (audioPlayerService.isPlaying()) {
                    audioPlayerService.pause();
                    audioPlayerService.sendUpdateMessage();
                } else {
                    audioPlayerService.play();
                    audioPlayerService.sendUpdateMessage();
                }
            } else if (ACTION_PREV.equals(action)) {
                audioPlayerService.playPrev();
            } else if (ACTION_NEXT.equals(action)) {
                audioPlayerService.playNext();
            }
        } else {
            Log.e("NotificationService", "AudioPlayerService is not bound");
        }

        return START_STICKY;
    }
}