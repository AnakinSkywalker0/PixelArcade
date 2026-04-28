package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar sbVolume;
    private TextView tvVolumeLabel;
    private ImageView ivSoundEffects, ivMusic, ivAnimations, ivGridLines;
    private LinearLayout rowSoundEffects, rowMusic, rowAnimations, rowGridLines;
    private Button btnResetData, btnBackToMenu;

    private UserDataManager udm;
    private static final String PREFS_NAME = "PixelArcadePrefs";

    private boolean isSoundEffectsOn, isMusicOn, isAnimationsOn, isGridLinesOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settingsContent), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        udm = UserDataManager.getInstance(this);

        // Initialize Views
        tvVolumeLabel = findViewById(R.id.tvVolumeLabel);
        sbVolume = findViewById(R.id.sbVolume);

        ivSoundEffects = findViewById(R.id.ivSoundEffects);
        ivMusic = findViewById(R.id.ivMusic);
        ivAnimations = findViewById(R.id.ivAnimations);
        ivGridLines = findViewById(R.id.ivGridLines);

        rowSoundEffects = findViewById(R.id.rowSoundEffects);
        rowMusic = findViewById(R.id.rowMusic);
        rowAnimations = findViewById(R.id.rowAnimations);
        rowGridLines = findViewById(R.id.rowGridLines);

        btnResetData = findViewById(R.id.btnResetData);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        loadSettings();

        // Listeners
        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvVolumeLabel.setText("VOLUME " + progress + "%");
                udm.putInt("volume", progress);
                SoundManager.getInstance(SettingsActivity.this).loadPreferences();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

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

        rowGridLines.setOnClickListener(v -> {
            isGridLinesOn = !isGridLinesOn;
            updateToggle(ivGridLines, isGridLinesOn, "grid_lines");
        });

        btnResetData.setOnClickListener(v -> resetData());
        btnBackToMenu.setOnClickListener(v -> finish());
    }

    private void updateToggle(ImageView iv, boolean isOn, String prefKey) {
        iv.setImageResource(isOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
        udm.putBoolean(prefKey, isOn);
        SoundManager.getInstance(this).loadPreferences();
    }

    private void loadSettings() {
        int volume = udm.getInt("volume", 70);
        sbVolume.setProgress(volume);
        tvVolumeLabel.setText("VOLUME " + volume + "%");

        isSoundEffectsOn = udm.getBoolean("sound_effects", true);
        isMusicOn = udm.getBoolean("music", false);
        isAnimationsOn = udm.getBoolean("animations", true);
        isGridLinesOn = udm.getBoolean("grid_lines", true);

        ivSoundEffects.setImageResource(isSoundEffectsOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
        ivMusic.setImageResource(isMusicOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
        ivAnimations.setImageResource(isAnimationsOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
        ivGridLines.setImageResource(isGridLinesOn ? R.drawable.ic_toggle_on : R.drawable.ic_toggle_off);
    }

    private void resetData() {
        udm.remove("high_score_2048");
        udm.remove("plays_2048");
        loadSettings();
        Toast.makeText(this, "Game data reset!", Toast.LENGTH_SHORT).show();
    }
}
