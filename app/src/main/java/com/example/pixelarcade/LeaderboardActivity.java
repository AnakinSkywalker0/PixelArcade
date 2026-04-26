package com.example.pixelarcade;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private List<LeaderboardEntry> galagaData, data2048, tttData, allData;
    private AppCompatButton chipGalaga, chip2048, chipTtt, chipAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leaderboard);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.leaderboardRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());

        // Initialize Views
        rvLeaderboard = findViewById(R.id.rvLeaderboard);
        chipGalaga = findViewById(R.id.chipGalaga);
        chip2048 = findViewById(R.id.chip2048);
        chipTtt = findViewById(R.id.chipTtt);
        chipAll = findViewById(R.id.chipAll);

        // Setup Mock Data
        setupMockData();

        // Setup RecyclerView
        adapter = new LeaderboardAdapter(allData);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this));
        rvLeaderboard.setAdapter(adapter);

        // Click Listeners
        chipGalaga.setOnClickListener(v -> selectCategory("GALAGA"));
        chip2048.setOnClickListener(v -> selectCategory("2048"));
        chipTtt.setOnClickListener(v -> selectCategory("TTT"));
        chipAll.setOnClickListener(v -> selectCategory("ALL"));

        // Default Selection
        selectCategory("ALL");
    }

    private void setupMockData() {
        galagaData = new ArrayList<>();
        galagaData.add(new LeaderboardEntry(1, "G_COMMANDER", "45,000"));
        galagaData.add(new LeaderboardEntry(2, "SPACE_ACE", "38,500"));
        galagaData.add(new LeaderboardEntry(3, "NOVA_PILOT", "31,200"));
        galagaData.add(new LeaderboardEntry(4, "ALIEN_BANE", "28,000"));
        galagaData.add(new LeaderboardEntry(5, "STAR_LORD", "25,400"));

        data2048 = new ArrayList<>();
        data2048.add(new LeaderboardEntry(1, "GRID_MASTER", "131,072"));
        data2048.add(new LeaderboardEntry(2, "MATH_WIZ", "65,536"));
        data2048.add(new LeaderboardEntry(3, "TILE_FUSER", "32,768"));
        data2048.add(new LeaderboardEntry(4, "SLIDE_KING", "16,384"));
        data2048.add(new LeaderboardEntry(5, "LOGIC_PRO", "8,192"));

        tttData = new ArrayList<>();
        tttData.add(new LeaderboardEntry(1, "X_PERT", "150 Wins"));
        tttData.add(new LeaderboardEntry(2, "O_MEGA", "132 Wins"));
        tttData.add(new LeaderboardEntry(3, "NO_DRAW", "98 Wins"));
        tttData.add(new LeaderboardEntry(4, "TIC_TAC_TOE", "85 Wins"));
        tttData.add(new LeaderboardEntry(5, "TRI_BEAT", "72 Wins"));

        allData = new ArrayList<>();
        allData.add(new LeaderboardEntry(1, "PIXEL_KING", "Global #1"));
        allData.add(new LeaderboardEntry(2, "RETRO_GAL", "Global #2"));
        allData.add(new LeaderboardEntry(3, "ARCADE_WIZ", "Global #3"));
        allData.add(new LeaderboardEntry(4, "JOYSTICK_H", "Global #4"));
        allData.add(new LeaderboardEntry(5, "8BIT_HERO", "Global #5"));
        allData.add(new LeaderboardEntry(6, "NEON_CAT", "Global #6"));
        allData.add(new LeaderboardEntry(7, "BIT_CRUSHER", "Global #7"));
        allData.add(new LeaderboardEntry(8, "GLITCH_01", "Global #8"));
    }

    private void selectCategory(String category) {
        // Reset all chips to secondary/inactive style
        resetChips();

        switch (category) {
            case "GALAGA":
                chipGalaga.setBackgroundResource(R.drawable.bg_chip_black);
                chipGalaga.setTextColor(0xFFF6C547);
                adapter.updateData(galagaData);
                break;
            case "2048":
                chip2048.setBackgroundResource(R.drawable.bg_chip_orange);
                chip2048.setTextColor(0xFFFFFFFF);
                adapter.updateData(data2048);
                break;
            case "TTT":
                chipTtt.setBackgroundResource(R.drawable.bg_leaderboard_chip_active);
                chipTtt.setTextColor(0xFFF6C547);
                adapter.updateData(tttData);
                break;
            case "ALL":
                chipAll.setBackgroundResource(R.drawable.bg_leaderboard_chip_active);
                chipAll.setTextColor(0xFFFFFFFF);
                adapter.updateData(allData);
                break;
        }
    }

    private void resetChips() {
        chipGalaga.setBackgroundResource(R.drawable.bg_leaderboard_chip);
        chipGalaga.setTextColor(0xFF76716C);
        chip2048.setBackgroundResource(R.drawable.bg_leaderboard_chip);
        chip2048.setTextColor(0xFF76716C);
        chipTtt.setBackgroundResource(R.drawable.bg_leaderboard_chip);
        chipTtt.setTextColor(0xFF76716C);
        chipAll.setBackgroundResource(R.drawable.bg_leaderboard_chip);
        chipAll.setTextColor(0xFF76716C);
    }
}
