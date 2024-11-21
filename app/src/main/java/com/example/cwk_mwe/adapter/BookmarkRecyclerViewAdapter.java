package com.example.cwk_mwe.adapter;

import static com.example.cwk_mwe.utils.AppUtils.formatTime;
import static com.example.cwk_mwe.utils.Constants.ACTION_LOAD;
import static com.example.cwk_mwe.utils.Constants.ACTION_STOP;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cwk_mwe.R;
import com.example.cwk_mwe.service.AudioPlayerService;
import com.example.cwk_mwe.models.MusicCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookmarkRecyclerViewAdapter extends RecyclerView.Adapter<BookmarkRecyclerViewAdapter.MusicViewHolder> {

    private final List<MusicCard> bookmarkList;
    private final List<MusicCard> musicList;
    private final Context context;

    public BookmarkRecyclerViewAdapter(Context context, List<MusicCard> musicList) {
        this.context = context;
        this.bookmarkList = new ArrayList<>();
        this.musicList = musicList;
        loadBookmarksFromCache();
    }

    // This method reads the music cards from the SharedPreferences and adds them to the bookmark list.
    private void loadBookmarksFromCache() {
        SharedPreferences sharedPreferences = context.getSharedPreferences("BookmarkPrefs", Context.MODE_PRIVATE);
        String musicCardsJson = sharedPreferences.getString("musicCards", "[]");
        try {
            JSONArray musicCardsArray = new JSONArray(musicCardsJson);
            for (int i = 0; i < musicCardsArray.length(); i++) {
                JSONObject musicCardObject = musicCardsArray.getJSONObject(i);
                MusicCard musicCard = new MusicCard(
                        musicCardObject.getString("title"),
                        musicCardObject.getString("artist"),
                        musicCardObject.getString("album"),
                        musicCardObject.getString("duration"),
                        musicCardObject.getString("path"),
                        musicCardObject.getInt("progress")
                );
                bookmarkList.add(musicCard);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    // Load the music file and play it at the bookmarked progress when the user clicks on it
    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicCard music = bookmarkList.get(position);
        holder.title.setText(music.title);
        holder.artist.setText(music.artist);
        holder.progress.setText(String.format("at %s", formatTime(music.progress)));

        holder.itemView.setOnClickListener(v -> {
            // Stop the service if it is already running
            Intent stopIntent = new Intent(context, AudioPlayerService.class);
            stopIntent.setAction(ACTION_STOP);
            context.startService(stopIntent);

            // Use a delay to ensure the service has time to stop before starting a new one
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Load and play the new track
                Intent serviceIntent = new Intent(context, AudioPlayerService.class);
                serviceIntent.setAction(ACTION_LOAD);
                serviceIntent.putExtra("path", music.path);
                serviceIntent.putExtra("progress", music.progress);
                serviceIntent.putExtra("musicList", new ArrayList<>(musicList));
                context.startService(serviceIntent);
            }, 10); // Adjust the delay as needed
        });
    }

    @Override
    public int getItemCount() {
        return bookmarkList.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, progress;
        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.music_title);
            title.setSelected(true);
            artist = itemView.findViewById(R.id.music_artist);
            progress = itemView.findViewById(R.id.music_duration); // Assuming the same TextView is used for progress
        }
    }

    // This method clears the bookmark list and reloads the music cards from the SharedPreferences.
    public void reloadBookmarks() {
        bookmarkList.clear();
        loadBookmarksFromCache();
        notifyDataSetChanged();
    }
}