package com.example.pixelarcade;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.card.MaterialCardView;

public class GalagaLevelsActivity extends AppCompatActivity {

    private int selectedLevel = -1;
    private MaterialCardView[] levelCards = new MaterialCardView[4];
    private AppCompatButton btnPlayLevel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_galaga_levels);

        LinearLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        AppCompatButton btnMainMenu = findViewById(R.id.btnMainMenu);
        btnMainMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnPlayLevel = findViewById(R.id.btnPlayLevel);
        btnPlayLevel.setOnClickListener(v -> {
            if (selectedLevel != -1) {
                Intent intent = new Intent(this, GalagaWaveBeginActivity.class);
                intent.putExtra("LEVEL_NUMBER", selectedLevel);
                startActivity(intent);
            }
        });

        levelCards[0] = findViewById(R.id.cardLevel1);
        levelCards[1] = findViewById(R.id.cardLevel2);
        levelCards[2] = findViewById(R.id.cardLevel3);
        levelCards[3] = findViewById(R.id.cardLevel4);

        // Cards click handling moved to onResume
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.content.SharedPreferences prefs = getSharedPreferences("GalagaPrefs", MODE_PRIVATE);
        int unlockedLevels = prefs.getInt("UnlockedLevels", 1);

        for (int i = 0; i < 4; i++) {
            final int level = i + 1;
            if (levelCards[i] != null) {
                if (level <= unlockedLevels) {
                    levelCards[i].setOnClickListener(v -> selectLevel(level));
                    levelCards[i].setAlpha(1.0f);
                    levelCards[i].setCardBackgroundColor(android.graphics.Color.parseColor("#111111"));
                    
                    android.widget.TextView numTv = levelCards[i].findViewById(level == 1 ? R.id.tvNum1 : level == 2 ? R.id.tvNum2 : level == 3 ? R.id.tvNum3 : R.id.tvNum4);
                    if (numTv != null) numTv.setTextColor(android.graphics.Color.parseColor("#00FF7F"));
                } else {
                    levelCards[i].setOnClickListener(v -> Toast.makeText(this, "Level Locked!", Toast.LENGTH_SHORT).show());
                    levelCards[i].setAlpha(0.5f);
                }
            }
        }
    }

    private void selectLevel(int level) {
        selectedLevel = level;
        
        int defaultStroke = (int) (getResources().getDisplayMetrics().density * 1);
        int activeStroke = (int) (getResources().getDisplayMetrics().density * 2);

        android.content.SharedPreferences prefs = getSharedPreferences("GalagaPrefs", MODE_PRIVATE);
        int unlockedLevels = prefs.getInt("UnlockedLevels", 1);

        // Reset all cards
        for (int i = 0; i < 4; i++) {
            if (levelCards[i] != null) {
                // Locked levels stay dark grey, playable get standard grey
                levelCards[i].setStrokeColor(Color.parseColor(i < unlockedLevels ? "#222222" : "#1A1A1A"));
                levelCards[i].setStrokeWidth(defaultStroke);
            }
        }
        
        // Highlight the selected card with yellow
        if (levelCards[level - 1] != null) {
            levelCards[level - 1].setStrokeColor(Color.parseColor("#FFFF00"));
            levelCards[level - 1].setStrokeWidth(activeStroke);
        }

        // Enable the big yellow Play button at the bottom
        btnPlayLevel.setEnabled(true);
        btnPlayLevel.setAlpha(1.0f);
    }
}
