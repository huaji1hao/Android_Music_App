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

    public static final int ErrorCode = 1;
    public static final int SuccessCode = 0;
    public static final int MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO = 1; // Constant for permission request code

    public static void checkAndRequestPermissions(Activity activity, Runnable onPermissionGranted) {
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.READ_MEDIA_AUDIO)) {
                new AlertDialog.Builder(activity)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access the music files on your device.")
                        .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(activity,
                                new String[]{android.Manifest.permission.READ_MEDIA_AUDIO},
                                MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO))
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_MEDIA_AUDIO},
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO);
            }
        } else {
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
