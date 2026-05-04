package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.DailyRewardDialog;
import com.example.pixelarcade.galaga.GalagaMainMenuActivity;
import com.example.pixelarcade.game2048.GameLauncher2048Activity;
import com.example.pixelarcade.manager.UserDataManager;
import com.example.pixelarcade.ttt.TicTacToeLauncherActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final int DEFAULT_GRID_SIZE = 5;

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

        View tvProfileLink = findViewById(R.id.tvProfileLink);
        View tvLeaderboardLink = findViewById(R.id.tvLeaderboardLink);
        View profileHeader = findViewById(R.id.profileHeader);
        
        tvProfileLink.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        tvLeaderboardLink.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));
        if (profileHeader != null) {
            profileHeader.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        // Game Play Buttons
        View btnPlay2048 = findViewById(R.id.btnPlay2048);
        View btnPlaySpace = findViewById(R.id.btnPlaySpace);
        View btnPlayTicTacToe = findViewById(R.id.btnPlayTicTacToe);

        if (btnPlay2048 != null) {
            btnPlay2048.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GameLauncher2048Activity.class);
                startActivity(intent);
            });
        }

        if (btnPlaySpace != null) {
            btnPlaySpace.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, GalagaMainMenuActivity.class);
                startActivity(intent);
            });
        }

        if (btnPlayTicTacToe != null) {
            btnPlayTicTacToe.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, TicTacToeLauncherActivity.class);
                startActivity(intent);
            });
        }

        // Bottom Navigation
        View navChallenges = findViewById(R.id.navChallenges);
        View navHome = findViewById(R.id.navHome);
        View navShop = findViewById(R.id.navShop);
        View navSettings = findViewById(R.id.navSettings);

        if (navChallenges != null) {
            navChallenges.setOnClickListener(v -> startActivity(new Intent(this, DailyChallengesActivity.class)));
        }
        if (navShop != null) {
            navShop.setOnClickListener(v -> startActivity(new Intent(this, ShopActivity.class)));
        }
        if (navSettings != null) {
            navSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        }
        
        updateStats();
    }

    private void updateStats() {
        UserDataManager udm = UserDataManager.getInstance(this);
        
        // 1. Top Bar Stats
        int totalPlays = udm.getInt("plays_2048", 0) + udm.getInt("galaga_plays", 0) + udm.getInt("plays_ttt", 0);
        int streak = udm.getInt("streak_days", 0);
        int totalEarned = udm.getInt("total_coins_earned", 0);
        int currentCoins = udm.getInt("coins", 0);

        TextView tvTotalPlays = findViewById(R.id.tvTotalPlays);
        if (tvTotalPlays != null) tvTotalPlays.setText(String.format("%04d", totalPlays));

        TextView tvStreak = findViewById(R.id.tvStreakDays);
        if (tvStreak != null) tvStreak.setText(String.format("%02d DAYS", streak));

        TextView tvTotalEarned = findViewById(R.id.tvTotalEarned);
        if (tvTotalEarned != null) tvTotalEarned.setText(String.format("%06d", totalEarned));

        TextView tvCoinCount = findViewById(R.id.tvCoinCount);
        if (tvCoinCount != null) tvCoinCount.setText(String.valueOf(currentCoins));

        TextView tvAvatar = findViewById(R.id.playerAvatarEmoji);
        if (tvAvatar != null) {
            tvAvatar.setText(udm.getString("playerAvatarEmoji", "👾"));
        }

        // 2. Hall of Fame (New Section)
        int high2048 = udm.getInt("high_score_2048", 0);
        int highGalaga = udm.getInt("galaga_hi_score", 0);
        int winsTTT = udm.getInt("wins_ttt", 0);

        TextView tvBest2048 = findViewById(R.id.tvBest2048);
        if (tvBest2048 != null) tvBest2048.setText(String.valueOf(high2048));

        TextView tvBestGalaga = findViewById(R.id.tvBestGalaga);
        if (tvBestGalaga != null) tvBestGalaga.setText(String.valueOf(highGalaga));

        TextView tvBestTTT = findViewById(R.id.tvBestTTT);
        if (tvBestTTT != null) tvBestTTT.setText(String.valueOf(winsTTT));

        // 3. Game Card Stats
        TextView tv2048Stats = findViewById(R.id.tv2048Stats);
        if (tv2048Stats != null) tv2048Stats.setText("PLAYS " + udm.getInt("plays_2048", 0));

        TextView tvSpaceStats = findViewById(R.id.tvSpaceStats);
        if (tvSpaceStats != null) tvSpaceStats.setText("PLAYS " + udm.getInt("galaga_plays", 0));

        TextView tvTttStats = findViewById(R.id.tvTttStats);
        if (tvTttStats != null) tvTttStats.setText("PLAYS " + udm.getInt("plays_ttt", 0));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }
}