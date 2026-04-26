package com.example.pixelarcade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvPlayerName;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        tvPlayerName = findViewById(R.id.tvPlayerName);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        
        prefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);

        Button btnEditProfile = findViewById(R.id.btnEditProfile);
        Button btnViewLeaderboard = findViewById(R.id.btnViewLeaderboard);

        btnEditProfile.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        btnViewLeaderboard.setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class)));
        
        loadProfile();
    }

    private void loadProfile() {
        String name = prefs.getString("playerName", "BUDDY");
        tvPlayerName.setText(name);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }
}
