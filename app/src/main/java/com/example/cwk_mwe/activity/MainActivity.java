package com.example.cwk_mwe.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cwk_mwe.R;
import com.example.cwk_mwe.service.NotificationService;
import com.example.cwk_mwe.utils.AppUtils;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NotificationService.NOTIFICATION_PERMISSION_CODE);
            }
        }

        Button listButton = findViewById(R.id.list_button);
        listButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListActivity.class);
            intent.putExtra("MAIN", AppUtils.SuccessCode);
            startActivity(intent);
        });

        Button playerButton = findViewById(R.id.player_button);
        playerButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PlayerActivity.class);
            intent.putExtra("MAIN", AppUtils.SuccessCode);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        Button exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(v -> {
            finishAffinity(); // Close all activities and exit the app
            System.exit(0); // Ensure the app is completely closed
        });

    }


}
