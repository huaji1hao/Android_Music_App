package com.example.cwk_mwe.utils;

import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.Log;
import com.example.cwk_mwe.models.MusicCard;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.util.List;
import java.util.Locale;

public class AppUtils {
    /**
     * Load music files from the device's music directory
     * @param musicList The list to store the music files
     * @return A runnable to load the music files
     */
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
//                    musicCard.title = file.getName().substring(0, file.getName().lastIndexOf('.'));

                    // Retrieve metadata
                    try (MediaMetadataRetriever mmr = new MediaMetadataRetriever()) {
                        mmr.setDataSource(file.getAbsolutePath());
                        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                        String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                        String album = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                        String duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

                        musicCard.title = title != null ? title : file.getName().substring(0, file.getName().lastIndexOf('.'));
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

    // Helper method to format duration from milliseconds to mm:ss
    public static String formatTime(int timeInMillis) {
        int minutes = (timeInMillis / 1000) / 60;
        int seconds = (timeInMillis / 1000) % 60;
        return String.format(Locale.getDefault(),"%02d:%02d", minutes, seconds);
    }

    /**
     * Convert a music card to a JSON object
     * @param musicCard The music card to convert
     * @return The JSON object
     */
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
