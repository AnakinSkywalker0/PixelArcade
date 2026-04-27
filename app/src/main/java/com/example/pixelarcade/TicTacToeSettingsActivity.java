package com.example.pixelarcade;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TicTacToeSettingsActivity extends AppCompatActivity {

    private ImageView ivSoundEffects, ivMusic, ivAnimations, ivPlayerFirst;
    private LinearLayout rowSoundEffects, rowMusic, rowAnimations, rowPlayerFirst;
    private Button btnResetStats, btnBack;
    private TextView tvStatPlays, tvStatWins, tvStatWinRate;

    private SharedPreferences prefs;
    private static final String PREFS_NAME = "PixelArcadePrefs";

    private boolean isSoundEffectsOn, isMusicOn, isAnimationsOn, isPlayerFirstOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ttt_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.tttSettingsContent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Initialize Views
        ivSoundEffects = findViewById(R.id.ivTttSoundEffects);
        ivMusic = findViewById(R.id.ivTttMusic);
        ivAnimations = findViewById(R.id.ivTttAnimations);
        ivPlayerFirst = findViewById(R.id.ivTttPlayerFirst);

        rowSoundEffects = findViewById(R.id.rowTttSoundEffects);
        rowMusic = findViewById(R.id.rowTttMusic);
        rowAnimations = findViewById(R.id.rowTttAnimations);
        rowPlayerFirst = findViewById(R.id.rowTttPlayerFirst);

        btnResetStats = findViewById(R.id.btnTttResetStats);
        btnBack = findViewById(R.id.btnTttBackToMenu);

        tvStatPlays = findViewById(R.id.tvTttStatPlays);
        tvStatWins = findViewById(R.id.tvTttStatWins);
        tvStatWinRate = findViewById(R.id.tvTttStatWinRate);

        loadSettings();

        // Toggle listeners
        rowSoundEffects.setOnClickListener(v -> {
            isSoundEffectsOn = !isSoundEffectsOn;
            updateToggle(ivSoundEffects, isSoundEffectsOn, "sound_effects");
        });

        rowMusic.setOnClickListener(v -> {
            isMusicOn = !isMusicOn;
            updateToggle(ivMusic, isMusicOn, "music");
        });

        rowAnimations.setOnClickListener(v -> {
            isAnimationsOn = !isAnimationsOn;
            updateToggle(ivAnimations, isAnimationsOn, "animations");
        });

        rowPlayerFirst.setOnClickListener(v -> {
            isPlayerFirstOn = !isPlayerFirstOn;
            updateToggle(ivPlayerFirst, isPlayerFirstOn, "ttt_player_first");
        });

        btnResetStats.setOnClickListener(v -> resetTttData());
        btnBack.setOnClickListener(v -> finish());
    }

    private void updateToggle(ImageView iv, boolean isOn, String prefKey) {
        iv.setImageResource(isOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
        prefs.edit().putBoolean(prefKey, isOn).apply();
        SoundManager.getInstance(this).loadPreferences();
    }

    private void loadSettings() {
        isSoundEffectsOn = prefs.getBoolean("sound_effects", true);
        isMusicOn = prefs.getBoolean("music", false);
        isAnimationsOn = prefs.getBoolean("animations", true);
        isPlayerFirstOn = prefs.getBoolean("ttt_player_first", true);

        ivSoundEffects.setImageResource(isSoundEffectsOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
        ivMusic.setImageResource(isMusicOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
        ivAnimations.setImageResource(isAnimationsOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
        ivPlayerFirst.setImageResource(isPlayerFirstOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);

        // Load stats
        int plays = prefs.getInt("plays_ttt", 0);
        int wins = prefs.getInt("wins_ttt", 0);
        int winRate = plays > 0 ? (wins * 100 / plays) : 0;

        tvStatPlays.setText(String.valueOf(plays));
        tvStatWins.setText(String.valueOf(wins));
        tvStatWinRate.setText(winRate + "%");
    }

    private void resetTttData() {
        prefs.edit()
            .remove("plays_ttt")
            .remove("wins_ttt")
            .apply();
        loadSettings();
        Toast.makeText(this, "Game data reset!", Toast.LENGTH_SHORT).show();
    }
}
