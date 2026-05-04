package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity {

    private static final String TAG = "Leaderboard";
    private RecyclerView rvLeaderboard;
    private LeaderboardAdapter adapter;
    private TextView tvMyRank;
    private AppCompatButton chipGalaga, chip2048, chipTtt, chipAll;
    private String currentCategory = "ALL";

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
        tvMyRank = findViewById(R.id.tvMyRank);
        chipGalaga = findViewById(R.id.chipGalaga);
        chip2048 = findViewById(R.id.chip2048);
        chipTtt = findViewById(R.id.chipTtt);
        chipAll = findViewById(R.id.chipAll);

        // Setup RecyclerView
        adapter = new LeaderboardAdapter(new ArrayList<>());
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

    private void selectCategory(String category) {
        currentCategory = category;
        resetChips();

        switch (category) {
            case "GALAGA":
                chipGalaga.setBackgroundResource(R.drawable.bg_chip_black);
                chipGalaga.setTextColor(0xFFF6C547);
                fetchLeaderboard("galaga_hi_score", "Score");
                break;
            case "2048":
                chip2048.setBackgroundResource(R.drawable.bg_chip_orange);
                chip2048.setTextColor(0xFFFFFFFF);
                fetchLeaderboard("high_score_2048", "Score");
                break;
            case "TTT":
                chipTtt.setBackgroundResource(R.drawable.bg_leaderboard_chip_active);
                chipTtt.setTextColor(0xFFF6C547);
                fetchLeaderboard("wins_ttt", "Wins");
                break;
            case "ALL":
                chipAll.setBackgroundResource(R.drawable.bg_leaderboard_chip_active);
                chipAll.setTextColor(0xFFFFFFFF);
                fetchLeaderboard("total_coins_earned", "Coins");
                break;
        }
    }

    /**
     * Fetches top scores from Firestore, sorted descending by the given field.
     * Also injects the current user's own rank if they're not in the top 10.
     */
    private void fetchLeaderboard(String field, String suffix) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String myUid = currentUser != null ? currentUser.getUid() : null;

        db.collection("users")
            .orderBy(field, Query.Direction.DESCENDING)
            .limit(20)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<LeaderboardEntry> entries = new ArrayList<>();
                int rank = 1;
                boolean foundSelf = false;

                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    String name = doc.getString("playerName");
                    if (name == null || name.isEmpty()) name = "PLAYER";

                    Long val = doc.getLong(field);
                    long score = val != null ? val : 0;
                    if (score == 0) continue; // skip users with no score

                    String displayScore;
                    if (suffix.equals("Wins")) {
                        displayScore = score + " Wins";
                    } else if (suffix.equals("Coins")) {
                        displayScore = formatNumber(score) + " COINS";
                    } else {
                        displayScore = formatNumber(score);
                    }

                    boolean isMe = doc.getId().equals(myUid);
                    if (isMe) foundSelf = true;

                    entries.add(new LeaderboardEntry(rank, name.toUpperCase(), displayScore, isMe));
                    rank++;
                }

                // If current user not in top results, fetch their data and add at bottom
                if (!foundSelf && myUid != null) {
                    final int currentRank = rank;
                    db.collection("users").document(myUid).get()
                        .addOnSuccessListener(myDoc -> {
                            if (myDoc.exists()) {
                                String myName = myDoc.getString("playerName");
                                if (myName == null || myName.isEmpty()) myName = "YOU";
                                Long myVal = myDoc.getLong(field);
                                long myScore = myVal != null ? myVal : 0;

                                if (myScore > 0) {
                                    String myDisplay;
                                    if (suffix.equals("Wins")) {
                                        myDisplay = myScore + " Wins";
                                    } else if (suffix.equals("Coins")) {
                                        myDisplay = formatNumber(myScore) + " COINS";
                                    } else {
                                        myDisplay = formatNumber(myScore);
                                    }

                                    entries.add(new LeaderboardEntry(currentRank, myName.toUpperCase(), myDisplay, true));
                                    tvMyRank.setText("#" + currentRank);
                                } else {
                                    tvMyRank.setText("UNRANKED");
                                }
                            }
                            adapter.updateData(entries);
                        });
                } else {
                    if (foundSelf) {
                        for (LeaderboardEntry e : entries) {
                            if (e.isCurrentUser()) {
                                tvMyRank.setText("#" + e.getRank());
                                break;
                            }
                        }
                    } else {
                        tvMyRank.setText("UNRANKED");
                    }
                    adapter.updateData(entries);
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to fetch leaderboard", e);
                // Show empty state
                adapter.updateData(new ArrayList<>());
            });
    }

    private String formatNumber(long num) {
        if (num >= 1_000_000) return String.format("%.1fM", num / 1_000_000.0);
        if (num >= 1_000) return String.format("%.1fK", num / 1_000.0);
        return String.valueOf(num);
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
