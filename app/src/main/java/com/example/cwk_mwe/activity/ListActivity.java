package com.example.cwk_mwe.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cwk_mwe.utils.MusicCard;
import com.example.cwk_mwe.R;
import com.example.cwk_mwe.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends BaseActivity {
    private BookmarkRecyclerViewAdapter bookmarkAdapter;
    private MusicRecyclerViewAdapter adapter;
    private List<MusicCard> musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list);
        
        initializeMusicList();
        setupRecyclerViews();
        setupButtons();
    }

    private void initializeMusicList() {
        musicList = new ArrayList<>();
        AppUtils.checkAndRequestPermissions(this, AppUtils.loadMusicFiles(musicList));
    }

    private void setupRecyclerViews() {
        setupMusicRecyclerView();
        setupBookmarkRecyclerView();
    }

    private void setupMusicRecyclerView() {
        RecyclerView musicRecyclerView = findViewById(R.id.music_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }

            @Override
            public int getChildCount() {
                return Math.min(super.getChildCount(), 3); // Limit to 3 items
            }
        };
        musicRecyclerView.setLayoutManager(layoutManager);

        adapter = new MusicRecyclerViewAdapter(this, musicList);
        musicRecyclerView.setAdapter(adapter);
    }

    private void setupBookmarkRecyclerView() {
        RecyclerView bookmarkRecyclerView = findViewById(R.id.bookmark_recycler_view);
        LinearLayoutManager bookmarkLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }

            @Override
            public int getChildCount() {
                return Math.min(super.getChildCount(), 4); // Limit to 4 items
            }
        };
        bookmarkRecyclerView.setLayoutManager(bookmarkLayoutManager);
        bookmarkAdapter = new BookmarkRecyclerViewAdapter(this, musicList);
        bookmarkRecyclerView.setAdapter(bookmarkAdapter);
    }

    private void setupButtons() {
        ImageButton backButton = findViewById(R.id.back_button0);
        backButton.setOnClickListener(v -> finish());

        ImageView clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("AudioPlayerPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("musicCards");
            editor.apply();
            Log.d("PlayerActivity", "All bookmarks cleared");

            // Clear the bookmark list and notify the adapter
            bookmarkAdapter.clearBookmarks();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AppUtils.MY_PERMISSIONS_REQUEST_READ_MEDIA_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AppUtils.loadMusicFiles(musicList).run();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

}