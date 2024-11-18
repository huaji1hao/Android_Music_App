package com.example.cwk_mwe.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class AppUtils {
    public static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1; // Constant for permission request code
    public static final int PERMISSION_REQUEST_CODE = 1;
    public static final int MSG_UPDATE_MUSIC_INFO = 1;
    public static final int REQUEST_POST_NOTIFICATIONS = 1;
    public static final int NOTIFICATION_PERMISSION_CODE = 1001;

    public static final String ACTION_LOAD = "com.example.cwk_mwe.ACTION_LOAD";
    public static final String ACTION_STOP = "com.example.cwk_mwe.ACTION_STOP";

    public static void requestPermissionsAndRun(Activity activity, Runnable onPermissionGranted) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, execute the runnable
            onPermissionGranted.run();
        }
    }

    public static Runnable loadMusicFiles(List<MusicCard> musicList) {
        return () -> {
            File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

            // Filter and add music files to the music list
            File[] files = directory.listFiles((dir, name) -> {
                String lowerCaseName = name.toLowerCase();
                return lowerCaseName.endsWith(".mp3") || lowerCaseName.endsWith(".wav") ||
                        lowerCaseName.endsWith(".flac") || lowerCaseName.endsWith(".aac") ||
                        lowerCaseName.endsWith(".ogg");
            });

            if (files != null) {
                for (File file : files) {
                    Log.d("File", file.getName());
                    MusicCard musicCard = new MusicCard();

                    // Remove file extension from title
                    musicCard.title = file.getName().substring(0, file.getName().lastIndexOf('.'));

                    // Retrieve metadata
                    try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
                        mmr.setDataSource(file.getAbsolutePath());
                        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                        musicCard.artist = artist != null ? artist : "Unknown Artist";
                        musicCard.album = album != null ? album : "Unknown Album";
                        musicCard.duration = duration != null ? duration : "Unknown Duration";
                        musicCard.path = file.getAbsolutePath();
                    } catch (Exception e) {
                        Log.e("MediaMetadataRetriever", "Error retrieving metadata", e);
                    }

                    musicList.add(musicCard);
                }
            } else {
                Log.d("File", "No files found in the directory");
            }
        };
    }

    // Helper method to format duration from milliseconds to mm:ss
    public static String formatTime(String duration) {
        long millis = Long.parseLong(duration);
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public static String formatTime(int timeInMillis) {
        int minutes = (timeInMillis / 1000) / 60;
        int seconds = (timeInMillis / 1000) % 60;
        return String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
    }


    public static JSONObject musicCardToJson(MusicCard musicCard) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("title", musicCard.title);
            jsonObject.put("artist", musicCard.artist);
            jsonObject.put("album", musicCard.album);
            jsonObject.put("duration", musicCard.duration);
            jsonObject.put("path", musicCard.path);
            jsonObject.put("progress", musicCard.progress);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
