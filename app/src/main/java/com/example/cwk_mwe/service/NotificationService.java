package com.example.cwk_mwe.service;

import static com.example.cwk_mwe.utils.Constants.*;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.cwk_mwe.R;

public class NotificationService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    // Handle the notification service's intents
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent == null || intent.getAction() == null) {
            return START_STICKY;
        }

        String action = intent.getAction();
        if (ACTION_SHOW_NOTIFICATION.equals(action)) {
            showNotification();
        } else if (ACTION_HIDE_NOTIFICATION.equals(action)) {
            stopSelf();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Create the audio player notification channel
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

    // Show the notification
    private void showNotification() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Audiobook Player")
                .setContentText("Playing audiobook")
                .setSmallIcon(R.drawable.ic_audiobook) // Replace with your icon
                .setOngoing(true) // Make the notification ongoing
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }
}