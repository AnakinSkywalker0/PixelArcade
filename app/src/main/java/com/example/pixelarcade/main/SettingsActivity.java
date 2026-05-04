package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;
import com.example.pixelarcade.manager.SoundManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import com.example.pixelarcade.auth.AuthActivity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SettingsActivity extends AppCompatActivity {

    private SeekBar sbVolume;
    private TextView tvVolumeLabel;
    private ImageView ivSoundEffects, ivMusic, ivAnimations, ivGridLines;
    private TextView tvToggleSfx, tvToggleMusic, tvToggleAnim, tvToggleGrid;
    private LinearLayout rowSoundEffects, rowMusic, rowAnimations, rowGridLines;
    private View btnResetData, btnBackToMenu, btnBack;

    private UserDataManager udm;

    private boolean isSoundEffectsOn, isMusicOn, isAnimationsOn, isGridLinesOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigation), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        udm = UserDataManager.getInstance(this);

        // Header
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        setupNavigation();

        TextView tvCoins = findViewById(R.id.tvSettingsCoins);
        if (tvCoins != null) {
            tvCoins.setText(String.valueOf(udm.getInt("coins", 0)));
        }

        // Volume
        tvVolumeLabel = findViewById(R.id.tvVolumeLabel);
        sbVolume = findViewById(R.id.sbVolume);

        TextView btnVolMinus = findViewById(R.id.btnVolMinus);
        TextView btnVolPlus = findViewById(R.id.btnVolPlus);
        if (btnVolMinus != null) {
            btnVolMinus.setOnClickListener(v -> {
                int p = sbVolume.getProgress();
                if (p >= 10) sbVolume.setProgress(p - 10);
            });
        }
        if (btnVolPlus != null) {
            btnVolPlus.setOnClickListener(v -> {
                int p = sbVolume.getProgress();
                if (p <= 90) sbVolume.setProgress(p + 10);
            });
        }

        // Toggles
        ivSoundEffects = findViewById(R.id.ivSoundEffects);
        ivMusic = findViewById(R.id.ivMusic);
        ivAnimations = findViewById(R.id.ivAnimations);
        ivGridLines = findViewById(R.id.ivGridLines);

        tvToggleSfx = findViewById(R.id.tvToggleSfx);
        tvToggleMusic = findViewById(R.id.tvToggleMusic);
        tvToggleAnim = findViewById(R.id.tvToggleAnim);
        tvToggleGrid = findViewById(R.id.tvToggleGrid);

        rowSoundEffects = findViewById(R.id.rowSoundEffects);
        rowMusic = findViewById(R.id.rowMusic);
        rowAnimations = findViewById(R.id.rowAnimations);
        rowGridLines = findViewById(R.id.rowGridLines);

        btnResetData = findViewById(R.id.btnResetData);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);

        loadSettings();

        // Volume listener
        sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                udm.putInt("volume", progress);
                SoundManager.getInstance(SettingsActivity.this).loadPreferences();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Toggle listeners
        rowSoundEffects.setOnClickListener(v -> {
            isSoundEffectsOn = !isSoundEffectsOn;
            updateToggle(ivSoundEffects, tvToggleSfx, isSoundEffectsOn, "sound_effects");
        });

        rowMusic.setOnClickListener(v -> {
            isMusicOn = !isMusicOn;
            updateToggle(ivMusic, tvToggleMusic, isMusicOn, "music");
        });

        rowAnimations.setOnClickListener(v -> {
            isAnimationsOn = !isAnimationsOn;
            updateToggle(ivAnimations, tvToggleAnim, isAnimationsOn, "animations");
        });

        rowGridLines.setOnClickListener(v -> {
            isGridLinesOn = !isGridLinesOn;
            updateToggle(ivGridLines, tvToggleGrid, isGridLinesOn, "grid_lines");
        });

        btnResetData.setOnClickListener(v -> resetData());
        btnBackToMenu.setOnClickListener(v -> finish());
    }

    private void setupNavigation() {
        findViewById(R.id.navArcade).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navChallenges).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, DailyChallengesActivity.class));
            finish();
        });
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navShop).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, ShopActivity.class));
            finish();
        });
    }

    private void updateToggle(ImageView iv, TextView tvLabel, boolean isOn, String prefKey) {
        if (isOn) {
            iv.setImageResource(R.drawable.bg_toggle_on);
            tvLabel.setText("ON");
            tvLabel.setTextColor(0xFFFFFFFF);
            tvLabel.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.START);
            tvLabel.setPadding(dpToPx(8), 0, dpToPx(28), dpToPx(3));
        } else {
            iv.setImageResource(R.drawable.bg_toggle_off);
            tvLabel.setText("OFF");
            tvLabel.setTextColor(0xFFAAAAAA);
            tvLabel.setGravity(android.view.Gravity.CENTER_VERTICAL | android.view.Gravity.END);
            tvLabel.setPadding(dpToPx(28), 0, dpToPx(8), dpToPx(3));
        }
        udm.putBoolean(prefKey, isOn);
        SoundManager.getInstance(this).loadPreferences();
    }

    private void loadSettings() {
        int volume = udm.getInt("volume", 70);
        sbVolume.setProgress(volume);

        isSoundEffectsOn = udm.getBoolean("sound_effects", true);
        isMusicOn = udm.getBoolean("music", false);
        isAnimationsOn = udm.getBoolean("animations", true);
        isGridLinesOn = udm.getBoolean("grid_lines", true);

        updateToggle(ivSoundEffects, tvToggleSfx, isSoundEffectsOn, "sound_effects");
        updateToggle(ivMusic, tvToggleMusic, isMusicOn, "music");
        updateToggle(ivAnimations, tvToggleAnim, isAnimationsOn, "animations");
        updateToggle(ivGridLines, tvToggleGrid, isGridLinesOn, "grid_lines");
    }

    private void resetData() {
        udm.clearAllData();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}
