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

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvPlayerName, tvSubTitle, tvMemberSince, tvProfileAvatarEmoji;
    private TextView tvTotalCoinsEarned, tvHighScore, tvTotalGames;
    private TextView tv2048Plays, tv2048High, tv2048Coins;
    private TextView tvTttPlays, tvTttWins, tvTttWinRate;
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

        prefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);

        // Initialize Views
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvProfileAvatarEmoji = findViewById(R.id.tvProfileAvatarEmoji);

        // Top stats row
        tvTotalCoinsEarned = findViewById(R.id.tvTotalCoinsEarned);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvTotalGames = findViewById(R.id.tvTotalGames);

        // 2048 breakdown
        tv2048Plays = findViewById(R.id.tv2048Plays);
        tv2048High = findViewById(R.id.tv2048High);
        tv2048Coins = findViewById(R.id.tv2048Coins);

        // TTT breakdown
        tvTttPlays = findViewById(R.id.tvTttPlays);
        tvTttWins = findViewById(R.id.tvTttWins);
        tvTttWinRate = findViewById(R.id.tvTttWinRate);

        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Action buttons
        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.btnViewLeaderboard).setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class)));

        loadProfile();
    }

    private void loadProfile() {
        // Player name
        String name = prefs.getString("playerName", "BUDDY");
        tvPlayerName.setText(name);

        // Player Tagline (Custom or default based on stats)
        String tagline = prefs.getString("playerTagline", null);
        if (tagline == null || tagline.isEmpty()) {
            int totalGames = prefs.getInt("plays_2048", 0) + prefs.getInt("plays_ttt", 0);
            if (totalGames >= 100) tagline = "ARCADE LEGEND";
            else if (totalGames >= 50) tagline = "ARCADE MASTER";
            else if (totalGames >= 20) tagline = "ARCADE WARRIOR";
            else if (totalGames >= 5) tagline = "ARCADE ROOKIE";
            else tagline = "NEW PLAYER";
        }
        tvSubTitle.setText(tagline);

        // Avatar Emoji
        String avatarEmoji = prefs.getString("playerAvatarEmoji", "👾");
        tvProfileAvatarEmoji.setText(avatarEmoji);

        // Member since
        String joinDate = prefs.getString("join_date", null);
        if (joinDate == null) {
            joinDate = new java.text.SimpleDateFormat("MMM yyyy", Locale.US).format(new java.util.Date());
            prefs.edit().putString("join_date", joinDate).apply();
        }
        tvMemberSince.setText("MEMBER SINCE " + joinDate.toUpperCase());

        // Stats
        int currentCoins = prefs.getInt("coins", 0);
        tvTotalCoinsEarned.setText(formatNumber(currentCoins));

        int highScore2048 = prefs.getInt("high_score_2048", 0);
        tvHighScore.setText(formatNumber(highScore2048));

        int totalGamesCount = prefs.getInt("plays_2048", 0) + prefs.getInt("plays_ttt", 0);
        tvTotalGames.setText(String.valueOf(totalGamesCount));

        // 2048 Breakdown
        int plays2048 = prefs.getInt("plays_2048", 0);
        int coins2048 = prefs.getInt("coins_earned_2048", 0);
        tv2048Plays.setText(String.valueOf(plays2048));
        tv2048High.setText(formatNumber(highScore2048));
        tv2048Coins.setText(String.valueOf(coins2048));

        // TTT Breakdown
        int playsTtt = prefs.getInt("plays_ttt", 0);
        int winsTtt = prefs.getInt("wins_ttt", 0);
        int winRate = playsTtt > 0 ? (winsTtt * 100 / playsTtt) : 0;

        tvTttPlays.setText(String.valueOf(playsTtt));
        tvTttWins.setText(String.valueOf(winsTtt));
        tvTttWinRate.setText(winRate + "%");
    }

    private String formatNumber(int num) {
        if (num >= 1000) {
            return String.format(Locale.US, "%.1fK", num / 1000.0);
        }
        return String.valueOf(num);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }
}
