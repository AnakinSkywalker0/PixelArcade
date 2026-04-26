package com.example.pixelarcade;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GameLauncher2048Activity extends AppCompatActivity {

    private Button btnGrid4x4;
    private Button btnGrid5x5;
    private Button btnGrid6x6;
    private Button btnStartGame;
    private Button btnSettings;
    private ImageButton btnBack;

    // Overlay Views
    private android.view.View overlayHowToPlay;
    private Button btnPlayNow;
    private android.widget.TextView btnCancelTutorial;

    private int selectedGridSize = 5; // Default grid size

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_2048_launcher);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        btnGrid4x4 = findViewById(R.id.btnGrid4x4);
        btnGrid5x5 = findViewById(R.id.btnGrid5x5);
        btnGrid6x6 = findViewById(R.id.btnGrid6x6);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnSettings = findViewById(R.id.btnSettings);
        btnBack = findViewById(R.id.btnBack);

        overlayHowToPlay = findViewById(R.id.overlayHowToPlay);
        btnPlayNow = findViewById(R.id.btnPlayNow);
        btnCancelTutorial = findViewById(R.id.btnCancelTutorial);

        // Set click listeners for grid selection
        btnGrid4x4.setOnClickListener(v -> selectGrid(4));
        btnGrid5x5.setOnClickListener(v -> selectGrid(5));
        btnGrid6x6.setOnClickListener(v -> selectGrid(6));

        // Start Game Button click - Now triggers tutorial
        btnStartGame.setOnClickListener(v -> {
            overlayHowToPlay.setVisibility(android.view.View.VISIBLE);
        });

        // Play Now Button inside overlay - Actually launches the game
        btnPlayNow.setOnClickListener(v -> {
            overlayHowToPlay.setVisibility(android.view.View.GONE);
            android.content.Intent intent = new android.content.Intent(GameLauncher2048Activity.this, GameActivity.class);
            intent.putExtra("GRID_SIZE", selectedGridSize);
            startActivity(intent);
        });

        // Cancel Tutorial
        btnCancelTutorial.setOnClickListener(v -> {
            overlayHowToPlay.setVisibility(android.view.View.GONE);
        });

        // Tapping background also cancels tutorial
        overlayHowToPlay.setOnClickListener(v -> {
            overlayHowToPlay.setVisibility(android.view.View.GONE);
        });

        // Back button click (Since this comes from Home Screen, finish this activity)
        btnBack.setOnClickListener(v -> {
            finish();
        });

        // Settings button click
        btnSettings.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(GameLauncher2048Activity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private void selectGrid(int size) {
        selectedGridSize = size;
        
        // Reset all buttons to unselected state
        btnGrid4x4.setBackgroundResource(R.drawable.bg_btn_grid_unselected);
        btnGrid5x5.setBackgroundResource(R.drawable.bg_btn_grid_unselected);
        btnGrid6x6.setBackgroundResource(R.drawable.bg_btn_grid_unselected);
        
        // Highlight the selected button
        if (size == 4) {
            btnGrid4x4.setBackgroundResource(R.drawable.bg_btn_grid_selected);
        } else if (size == 5) {
            btnGrid5x5.setBackgroundResource(R.drawable.bg_btn_grid_selected);
        } else if (size == 6) {
            btnGrid6x6.setBackgroundResource(R.drawable.bg_btn_grid_selected);
        }
    }
}