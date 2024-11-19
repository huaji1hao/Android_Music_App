package com.example.cwk_mwe.adapter;

import static com.example.cwk_mwe.utils.AppUtils.formatTime;
import static com.example.cwk_mwe.utils.Constants.ACTION_LOAD;
import static com.example.cwk_mwe.utils.Constants.ACTION_STOP;

import android.content.Context;
import android.content.Intent;
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
import com.example.cwk_mwe.utils.AppUtils;
import com.example.cwk_mwe.models.MusicCard;

import java.util.ArrayList;
import java.util.List;

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicViewHolder> {
    private final List<MusicCard> musicList;
    private final Context context;

    public MusicRecyclerViewAdapter(Context context, List<MusicCard> musicList) {
        this.context = context;
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    // Load the music file and play it when the user clicks on it
    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicCard music = musicList.get(position);
        holder.title.setText(music.title);
        holder.artist.setText(music.artist);
        holder.duration.setText(formatTime(music.duration));

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
                serviceIntent.putExtra("musicList", new ArrayList<>(musicList));
                context.startService(serviceIntent);
            }, 10); // Adjust the delay as needed
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public static class MusicViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, duration;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.music_title);
            artist = itemView.findViewById(R.id.music_artist);
            duration = itemView.findViewById(R.id.music_duration);
        }
    }

    // Clear the list and reload the music files
    public void reloadMusicList() {
        musicList.clear();
        AppUtils.loadMusicFiles(musicList).run();
        notifyDataSetChanged();
    }
}