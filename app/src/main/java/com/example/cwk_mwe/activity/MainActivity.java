package com.example.cwk_mwe.activity;


import static com.example.cwk_mwe.utils.AppUtils.PERMISSION_REQUEST_CODE;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cwk_mwe.utils.MusicCard;
import com.example.cwk_mwe.R;
import com.example.cwk_mwe.utils.VerticalSpaceItemDecoration;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private BookmarkRecyclerViewAdapter bookmarkAdapter;
    private MusicRecyclerViewAdapter musicAdapter;
    private List<MusicCard> musicList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        setupRecyclerViews();
        setupButtons();
        setupBottomNavigation();
        handlePermissions();
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
        musicList = new ArrayList<>();
        musicAdapter = new MusicRecyclerViewAdapter(this, musicList);
        musicRecyclerView.setAdapter(musicAdapter);

        int verticalSpaceHeight = getResources().getDimensionPixelSize(R.dimen.recycler_view_item_space);
        musicRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(verticalSpaceHeight));

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

        int verticalSpaceHeight = getResources().getDimensionPixelSize(R.dimen.recycler_view_item_space);
        bookmarkRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(verticalSpaceHeight));
    }

    private void setupButtons() {
        ImageView clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> {
            SharedPreferences sharedPreferences = getSharedPreferences("BookmarkPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            Log.d("PlayerActivity", "All bookmarks cleared");

            // Clear the bookmark list and notify the adapter
            bookmarkAdapter.reloadBookmarks();
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
        List<String> permissionsNeeded = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_CALL_LOG);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(Manifest.permission.READ_MEDIA_AUDIO);
        }

        if (!permissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // All permissions are granted, load the music files
                musicAdapter.reloadMusicList();
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

        musicAdapter.reloadMusicList();
        bookmarkAdapter.reloadBookmarks();
    }

}