package com.example.pixelarcade.ttt;

import com.example.pixelarcade.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TicTacToeLauncherActivity extends AppCompatActivity {

    private Button btnDiffEasy, btnDiffMedium, btnDiffHard;
    private Button btnStartGame, btnSettings;
    private ImageButton btnBack;

    // Overlay Views
    private View overlayHowToPlay;
    private Button btnPlayNow;
    private TextView btnCancelTutorial;

    // 0 = Easy, 1 = Medium, 2 = Hard
    private int selectedDifficulty = 1; // Default: Medium

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ttt_launcher);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        btnDiffEasy = findViewById(R.id.btnDiffEasy);
        btnDiffMedium = findViewById(R.id.btnDiffMedium);
        btnDiffHard = findViewById(R.id.btnDiffHard);
        btnStartGame = findViewById(R.id.btnTttStartGame);
        btnSettings = findViewById(R.id.btnTttSettings);
        btnBack = findViewById(R.id.btnTttBack);

        overlayHowToPlay = findViewById(R.id.overlayTttHowToPlay);
        btnPlayNow = findViewById(R.id.btnTttPlayNow);
        btnCancelTutorial = findViewById(R.id.btnTttCancelTutorial);

        // Difficulty selectors
        btnDiffEasy.setOnClickListener(v -> selectDifficulty(0));
        btnDiffMedium.setOnClickListener(v -> selectDifficulty(1));
        btnDiffHard.setOnClickListener(v -> selectDifficulty(2));

        // Start Game -> Show How to Play overlay
        btnStartGame.setOnClickListener(v -> {
            overlayHowToPlay.setVisibility(View.VISIBLE);
        });

        // Play Now -> Launch the actual game
        btnPlayNow.setOnClickListener(v -> {
            overlayHowToPlay.setVisibility(View.GONE);
            Intent intent = new Intent(TicTacToeLauncherActivity.this, TicTacToeActivity.class);
            intent.putExtra("DIFFICULTY", selectedDifficulty);
            startActivity(intent);
        });

        // Cancel tutorial
        btnCancelTutorial.setOnClickListener(v -> {
            overlayHowToPlay.setVisibility(View.GONE);
        });

        // Tapping overlay background cancels it
        overlayHowToPlay.setOnClickListener(v -> {
            overlayHowToPlay.setVisibility(View.GONE);
        });

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Settings button
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(TicTacToeLauncherActivity.this, TicTacToeSettingsActivity.class);
            startActivity(intent);
        });
    }

    private void selectDifficulty(int difficulty) {
        selectedDifficulty = difficulty;

        // Reset all buttons to unselected
        btnDiffEasy.setBackgroundResource(R.drawable.bg_ttt_btn_unselected);
        btnDiffMedium.setBackgroundResource(R.drawable.bg_ttt_btn_unselected);
        btnDiffHard.setBackgroundResource(R.drawable.bg_ttt_btn_unselected);
        btnDiffEasy.setTextColor(getResources().getColor(R.color.ttt_title_color));
        btnDiffMedium.setTextColor(getResources().getColor(R.color.ttt_title_color));
        btnDiffHard.setTextColor(getResources().getColor(R.color.ttt_title_color));

        // Highlight selected
        switch (difficulty) {
            case 0:
                btnDiffEasy.setBackgroundResource(R.drawable.bg_ttt_btn_selected);
                btnDiffEasy.setTextColor(getResources().getColor(R.color.white));
                break;
            case 1:
                btnDiffMedium.setBackgroundResource(R.drawable.bg_ttt_btn_selected);
                btnDiffMedium.setTextColor(getResources().getColor(R.color.white));
                break;
            case 2:
                btnDiffHard.setBackgroundResource(R.drawable.bg_ttt_btn_selected);
                btnDiffHard.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }
}
