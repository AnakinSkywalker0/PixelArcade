package com.example.pixelarcade;

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
        // Initialize Sound (removed for non-Galaga)
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvProfileLink = findViewById(R.id.tvProfileLink);
        TextView tvLeaderboardLink = findViewById(R.id.tvLeaderboardLink);
        View coinBadge = findViewById(R.id.coinBadge);
        Button btnPlay2048 = findViewById(R.id.btnPlay2048);
        Button btnPlaySpace = findViewById(R.id.btnPlaySpace);
        Button btnPlayTicTacToe = findViewById(R.id.btnPlayTicTacToe);

        tvProfileLink.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        tvLeaderboardLink.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));
        coinBadge.setOnClickListener(v -> startActivity(new Intent(this, DailyChallengesActivity.class)));

        btnPlay2048.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameLauncher2048Activity.class);
            startActivity(intent);
        });

        btnPlaySpace.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GalagaMainMenuActivity.class);
            startActivity(intent);
        });

        btnPlayTicTacToe.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TicTacToeLauncherActivity.class);
            startActivity(intent);
        });
        
        TextView btnDailyReward = findViewById(R.id.btnDailyReward);
        if (btnDailyReward != null) {
            btnDailyReward.setOnClickListener(v -> {
                showDailyRewardDialog(); // Always let them see their streak!
            });
        }
        
        updateStats();
        checkDailyReward();
    }

    private boolean isDailyRewardReady() {
        android.content.SharedPreferences prefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);
        long lastClaimTime = prefs.getLong("last_daily_claim", 0);
        if (lastClaimTime == 0) return true;

        java.util.Calendar lastClaim = java.util.Calendar.getInstance();
        lastClaim.setTimeInMillis(lastClaimTime);
        lastClaim.set(java.util.Calendar.HOUR_OF_DAY, 0);
        lastClaim.set(java.util.Calendar.MINUTE, 0);
        lastClaim.set(java.util.Calendar.SECOND, 0);
        lastClaim.set(java.util.Calendar.MILLISECOND, 0);

        java.util.Calendar today = java.util.Calendar.getInstance();
        today.set(java.util.Calendar.HOUR_OF_DAY, 0);
        today.set(java.util.Calendar.MINUTE, 0);
        today.set(java.util.Calendar.SECOND, 0);
        today.set(java.util.Calendar.MILLISECOND, 0);

        long diffMillis = today.getTimeInMillis() - lastClaim.getTimeInMillis();
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);
        
        return diffDays > 0;
    }

    private void checkDailyReward() {
        if (isDailyRewardReady()) {
            showDailyRewardDialog();
        }
    }

    private void showDailyRewardDialog() {
        DailyRewardDialog dialog = new DailyRewardDialog(this, coins -> {
            updateStats(); // Refresh the coin UI
        });
        dialog.show();
    }

    private void updateStats() {
        android.content.SharedPreferences prefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);
        
        // Coin Count
        int coins = prefs.getInt("coins", 0);
        TextView tvCoinCount = findViewById(R.id.tvCoinCount);
        if (tvCoinCount != null) {
            tvCoinCount.setText(String.valueOf(coins));
        }

        // 2048 Stats
        int plays2048 = prefs.getInt("plays_2048", 0);
        int high2048 = prefs.getInt("high_score_2048", 0);
        TextView tv2048Stats = findViewById(R.id.tv2048Stats);
        if (tv2048Stats != null) {
            tv2048Stats.setText("PLAYS: " + plays2048 + " | HIGH: " + high2048);
        }

        // Space Stats (Placeholders for now)
        TextView tvSpaceStats = findViewById(R.id.tvSpaceStats);
        if (tvSpaceStats != null) {
            tvSpaceStats.setText("PLAYS: 0 | HIGH: 0");
        }

        // TTT Stats
        int playsTtt = prefs.getInt("plays_ttt", 0);
        int winsTtt = prefs.getInt("wins_ttt", 0);
        TextView tvTttStats = findViewById(R.id.tvTttStats);
        if (tvTttStats != null) {
            tvTttStats.setText("PLAYS: " + playsTtt + " | WINS: " + winsTtt);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }
}