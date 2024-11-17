package com.example.cwk_mwe.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cwk_mwe.service.NotificationService;
import com.example.cwk_mwe.utils.MusicCard;
import com.example.cwk_mwe.R;
import com.example.cwk_mwe.utils.AppUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private BookmarkRecyclerViewAdapter bookmarkAdapter;
    private MusicRecyclerViewAdapter adapter;
    private List<MusicCard> musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        handlePermissions();
        initializeMusicList();
        setupRecyclerViews();
        setupButtons();
        setupBottomNavigation();
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
                return Math.min(super.getChildCount(), 3); // Limit to 3 items
            }
        };
        bookmarkRecyclerView.setLayoutManager(bookmarkLayoutManager);
        bookmarkAdapter = new BookmarkRecyclerViewAdapter(this, musicList);
        bookmarkRecyclerView.setAdapter(bookmarkAdapter);
    }

    private void setupButtons() {
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

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_list) {
                // Already in ListActivity
                return true;
            } else if (itemId == R.id.navigation_player) {
                startActivity(new Intent(this, PlayerActivity.class));
                return true;
            } else if (itemId == R.id.navigation_settings) {
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            }
            return false;
        });
    }

    private void handlePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NotificationService.NOTIFICATION_PERMISSION_CODE);
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_list);
    }

}