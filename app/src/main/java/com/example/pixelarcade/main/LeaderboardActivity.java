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
                chipGalaga.setBackgroundResource(R.drawable.bg_pixel_card_dark);
                chipGalaga.setTextColor(0xFFFFFFFF);
                fetchLeaderboard("galaga_hi_score", "SCORE", "SCORE");
                break;
            case "2048":
                chip2048.setBackgroundResource(R.drawable.bg_pixel_card_dark);
                chip2048.setTextColor(0xFFFFFFFF);
                fetchLeaderboard("high_score_2048", "SCORE", "SCORE");
                break;
            case "TTT":
                chipTtt.setBackgroundResource(R.drawable.bg_pixel_card_dark);
                chipTtt.setTextColor(0xFFFFFFFF);
                fetchLeaderboard("wins_ttt", "WINS", "WINS");
                break;
            case "ALL":
                chipAll.setBackgroundResource(R.drawable.bg_pixel_card_dark);
                chipAll.setTextColor(0xFFF6C547);
                // Based on wins as requested
                fetchLeaderboard("wins_ttt", "WINS", "WINS");
                break;
        }
    }

    /**
     * Fetches top scores from Firestore, sorted descending by the given field.
     * Also injects the current user's own rank if they're not in the top 10.
     */
    private void fetchLeaderboard(String field, String suffix, String label) {
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
                    if (name == null || name.isEmpty()) {
                        String email = doc.getString("email");
                        if (email != null && !email.isEmpty()) {
                            name = email.split("@")[0];
                        } else {
                            name = "ANON_USER";
                        }
                    }

                    Long val = doc.getLong(field);
                    long score = val != null ? val : 0;
                    if (score == 0 && !field.equals("wins_ttt")) continue; // Only skip zero if not TTT wins

                    String displayScore = formatNumber(score);
                    boolean isMe = doc.getId().equals(myUid);
                    if (isMe) foundSelf = true;

                    entries.add(new LeaderboardEntry(rank, name.toUpperCase(), displayScore, label, isMe));
                    rank++;
                }

                // If current user not in top results, fetch their data and add at bottom
                if (!foundSelf && myUid != null) {
                    final int currentRank = rank;
                    db.collection("users").document(myUid).get()
                        .addOnSuccessListener(myDoc -> {
                            if (myDoc.exists()) {
                                String myName = myDoc.getString("playerName");
                                if (myName == null || myName.isEmpty()) {
                                    String email = myDoc.getString("email");
                                    if (email != null && !email.isEmpty()) {
                                        myName = email.split("@")[0];
                                    } else {
                                        myName = "YOU";
                                    }
                                }
                                Long myVal = myDoc.getLong(field);
                                long myScore = myVal != null ? myVal : 0;

                                if (myScore >= 0) {
                                    String myDisplay = formatNumber(myScore);
                                    entries.add(new LeaderboardEntry(currentRank, myName.toUpperCase(), myDisplay, label, true));
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
        chipGalaga.setBackgroundResource(R.drawable.bg_pixel_inner_dark);
        chipGalaga.setTextColor(0xFF5F564D);
        chip2048.setBackgroundResource(R.drawable.bg_pixel_inner_dark);
        chip2048.setTextColor(0xFF5F564D);
        chipTtt.setBackgroundResource(R.drawable.bg_pixel_inner_dark);
        chipTtt.setTextColor(0xFF5F564D);
        chipAll.setBackgroundResource(R.drawable.bg_pixel_inner_dark);
        chipAll.setTextColor(0xFF5F564D);
    }
}
