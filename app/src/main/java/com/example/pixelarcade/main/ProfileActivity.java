package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.SoundManager;
import com.example.pixelarcade.manager.UserDataManager;

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
    private TextView tvGalagaPlays, tvGalagaHigh, tvGalagaWave;
    private UserDataManager udm;

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

        udm = UserDataManager.getInstance(this);

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

        // Galaga breakdown
        tvGalagaPlays = findViewById(R.id.tvGalagaPlays);
        tvGalagaHigh = findViewById(R.id.tvGalagaHigh);
        tvGalagaWave = findViewById(R.id.tvGalagaWave);

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
        String name = udm.getString("playerName", "BUDDY");
        tvPlayerName.setText(name);

        // Player Tagline
        String tagline = udm.getString("playerTagline", null);
        if (tagline == null || tagline.isEmpty()) {
            int totalGames = udm.getInt("plays_2048", 0) + udm.getInt("plays_ttt", 0) + udm.getInt("galaga_plays", 0);
            if (totalGames >= 100) tagline = "ARCADE LEGEND";
            else if (totalGames >= 50) tagline = "ARCADE MASTER";
            else if (totalGames >= 20) tagline = "ARCADE WARRIOR";
            else if (totalGames >= 5) tagline = "ARCADE ROOKIE";
            else tagline = "NEW PLAYER";
        }
        tvSubTitle.setText(tagline);

        // Avatar Emoji
        String avatarEmoji = udm.getString("playerAvatarEmoji", "👾");
        tvProfileAvatarEmoji.setText(avatarEmoji);

        // Member since
        String joinDate = udm.getString("join_date", null);
        if (joinDate == null) {
            joinDate = new java.text.SimpleDateFormat("MMM yyyy", Locale.US).format(new java.util.Date());
            udm.putString("join_date", joinDate);
        }
        tvMemberSince.setText("MEMBER SINCE " + joinDate.toUpperCase());

        // Top Stats
        int totalEarned = udm.getInt("total_coins_earned", 0);
        tvTotalCoinsEarned.setText(formatNumber(totalEarned));

        int high2048 = udm.getInt("high_score_2048", 0);
        int highGalaga = udm.getInt("galaga_hi_score", 0);
        int highEndless = udm.getInt("galaga_endless_hi_score", 0);
        int overallBest = Math.max(high2048, Math.max(highGalaga, highEndless));
        tvHighScore.setText(formatNumber(overallBest));

        int totalGamesCount = udm.getInt("plays_2048", 0) + udm.getInt("plays_ttt", 0) + udm.getInt("galaga_plays", 0);
        tvTotalGames.setText(String.valueOf(totalGamesCount));

        // 2048 Breakdown
        int plays2048 = udm.getInt("plays_2048", 0);
        int coins2048 = udm.getInt("coins_earned_2048", 0);
        tv2048Plays.setText(String.valueOf(plays2048));
        tv2048High.setText(formatNumber(high2048));
        tv2048Coins.setText(String.valueOf(coins2048));

        // TTT Breakdown
        int playsTtt = udm.getInt("plays_ttt", 0);
        int winsTtt = udm.getInt("wins_ttt", 0);
        int winRate = playsTtt > 0 ? (winsTtt * 100 / playsTtt) : 0;
        tvTttPlays.setText(String.valueOf(playsTtt));
        tvTttWins.setText(String.valueOf(winsTtt));
        tvTttWinRate.setText(winRate + "%");

        // Galaga Breakdown
        int playsGalaga = udm.getInt("galaga_plays", 0);
        int waveGalaga = udm.getInt("galaga_endless_best_wave", 0);
        tvGalagaPlays.setText(String.valueOf(playsGalaga));
        tvGalagaHigh.setText(formatNumber(Math.max(highGalaga, highEndless)));
        tvGalagaWave.setText(String.valueOf(waveGalaga));
    }

    private String formatNumber(int num) {
        if (num >= 10000) {
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
