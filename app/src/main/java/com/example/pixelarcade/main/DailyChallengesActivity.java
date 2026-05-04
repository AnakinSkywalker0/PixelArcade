package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class DailyChallengesActivity extends AppCompatActivity {

    private UserDataManager udm;

    // Balance
    private TextView tvBalance;

    // Card 1 — 2048
    private ProgressBar progress1;
    private TextView tvProgress1;
    private TextView btnClaim1;

    // Card 2 — TTT Streak
    private ProgressBar progress2;
    private TextView tvProgress2;
    private TextView btnClaim2;

    // Card 3 — Galaga
    private ProgressBar progress3;
    private TextView tvProgress3;
    private TextView btnClaim3;

    // Colors per card (Premium Theme)
    private static final int COLOR_GOLD   = 0xFFFFD700;
    private static final int COLOR_RED    = 0xFFFF5252;
    private static final int COLOR_TEAL   = 0xFF26C6DA;
    private static final int COLOR_MUTED  = 0xFF555E70;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_challenges);

        // ConstraintLayout root ID is missing in my previous XML update, I should have added it or use a generic search
        // For now I'll just find the first view if possible or assume I added the ID in the next step.
        // Actually I'll just use android.R.id.content for insets if I'm unsure.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        udm = UserDataManager.getInstance(this);

        // Bind views
        tvBalance     = findViewById(R.id.tvChallengeBalance);

        progress1     = findViewById(R.id.progress1);
        tvProgress1   = findViewById(R.id.tvProgress1);
        btnClaim1     = findViewById(R.id.btnClaim1);

        progress2     = findViewById(R.id.progress2);
        tvProgress2   = findViewById(R.id.tvProgress2);
        btnClaim2     = findViewById(R.id.btnClaim2);

        progress3     = findViewById(R.id.progress3);
        tvProgress3   = findViewById(R.id.tvProgress3);
        btnClaim3     = findViewById(R.id.btnClaim3);

        findViewById(R.id.btnChallengesBack).setOnClickListener(v -> finish());

        setupNavigation();
        checkDailyReset();
        loadChallenges();
    }

    private void setupNavigation() {
        findViewById(R.id.navArcade).setOnClickListener(v -> {
            startActivity(android.content.Intent.createChooser(new android.content.Intent(this, MainActivity.class), "Select Home"));
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
        findViewById(R.id.navSettings).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, SettingsActivity.class));
            finish();
        });
    }

    private void checkDailyReset() {
        long lastReset = udm.getLong("challenge_last_reset", 0);
        Calendar lastCal = Calendar.getInstance();
        lastCal.setTimeInMillis(Math.max(lastReset, 1));
        lastCal.set(Calendar.HOUR_OF_DAY, 0);
        lastCal.set(Calendar.MINUTE, 0);
        lastCal.set(Calendar.SECOND, 0);
        lastCal.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        if (lastReset == 0 || today.getTimeInMillis() > lastCal.getTimeInMillis()) {
            Map<String, Object> reset = new HashMap<>();
            reset.put("challenge_512_done", false);
            reset.put("challenge_512_claimed", false);
            reset.put("challenge_ttt_streak_done", false);
            reset.put("challenge_ttt_streak_claimed", false);
            reset.put("challenge_ttt_consec_wins", 0);
            reset.put("challenge_2048_max_tile", 0);
            
            // Galaga reset
            reset.put("challenge_galaga_waves_done", false);
            reset.put("challenge_galaga_waves_claimed", false);
            
            reset.put("challenge_last_reset", System.currentTimeMillis());
            udm.putMultiple(reset);
        }
    }

    private void loadChallenges() {
        tvBalance.setText(String.valueOf(udm.getInt("coins", 0)));
        setup2048Challenge();
        setupTTTChallenge();
        setupGalagaChallenge();
    }

    private void setup2048Challenge() {
        boolean claimed = udm.getBoolean("challenge_512_claimed", false);
        boolean done    = udm.getBoolean("challenge_512_done", false);

        if (claimed) {
            progress1.setProgress(100);
            tvProgress1.setText("CLAIMED ✓");
            tvProgress1.setTextColor(COLOR_GOLD);
            setDoneState(btnClaim1, COLOR_GOLD);
        } else if (done) {
            progress1.setProgress(100);
            tvProgress1.setText("COMPLETED!");
            tvProgress1.setTextColor(COLOR_GOLD);
            setReadyState(btnClaim1, "CLAIM NOW", COLOR_GOLD);
            btnClaim1.setOnClickListener(v -> claimChallenge("challenge_512_claimed", 1));
        } else {
            int maxTile = udm.getInt("challenge_2048_max_tile", 0);
            int percent = (int)((Math.log(Math.max(2, maxTile))/Math.log(2)) / (Math.log(512)/Math.log(2)) * 100);
            progress1.setProgress(Math.min(100, percent));
            tvProgress1.setText(maxTile + "/512");
            tvProgress1.setTextColor(COLOR_MUTED);
            setLockedState(btnClaim1, "LOCKED", COLOR_MUTED);
        }
    }

    private void setupTTTChallenge() {
        boolean claimed = udm.getBoolean("challenge_ttt_streak_claimed", false);
        boolean done    = udm.getBoolean("challenge_ttt_streak_done", false);
        int consec      = udm.getInt("challenge_ttt_consec_wins", 0);

        if (claimed) {
            progress2.setProgress(100);
            tvProgress2.setText("CLAIMED ✓");
            tvProgress2.setTextColor(COLOR_RED);
            setDoneState(btnClaim2, COLOR_RED);
        } else if (done || consec >= 3) {
            progress2.setProgress(100);
            tvProgress2.setText("3/3 — COMPLETED!");
            tvProgress2.setTextColor(COLOR_RED);
            setReadyState(btnClaim2, "CLAIM NOW", COLOR_RED);
            btnClaim2.setOnClickListener(v -> claimChallenge("challenge_ttt_streak_claimed", 2));
        } else {
            int perc = (int)((consec / 3.0f) * 100);
            progress2.setProgress(perc);
            tvProgress2.setText(consec + " / 3 WINS");
            tvProgress2.setTextColor(COLOR_MUTED);
            setLockedState(btnClaim2, "LOCKED", COLOR_MUTED);
        }
    }

    private void setupGalagaChallenge() {
        boolean claimed = udm.getBoolean("challenge_galaga_waves_claimed", false);
        boolean done    = udm.getBoolean("challenge_galaga_waves_done", false);
        int bestWave    = udm.getInt("galaga_endless_best_wave", 0);
        int target      = 2;

        if (claimed) {
            progress3.setProgress(100);
            tvProgress3.setText("CLAIMED ✓");
            tvProgress3.setTextColor(COLOR_TEAL);
            setDoneState(btnClaim3, COLOR_TEAL);
        } else if (done || bestWave >= target) {
            if (!done) udm.putBoolean("challenge_galaga_waves_done", true);
            progress3.setProgress(100);
            tvProgress3.setText("WAVE 2 — COMPLETED!");
            tvProgress3.setTextColor(COLOR_TEAL);
            setReadyState(btnClaim3, "CLAIM NOW", COLOR_TEAL);
            btnClaim3.setOnClickListener(v -> claimChallenge("challenge_galaga_waves_claimed", 3));
        } else {
            int perc = (int)((bestWave / (float)target) * 100);
            progress3.setProgress(Math.min(100, perc));
            tvProgress3.setText("BEST: WAVE " + bestWave + "/" + target);
            tvProgress3.setTextColor(COLOR_MUTED);
            setLockedState(btnClaim3, "LOCKED", COLOR_MUTED);
        }
    }

    private void claimChallenge(String claimedKey, int cardNum) {
        int coins = udm.getInt("coins", 0);
        int totalEarned = udm.getInt("total_coins_earned", 0);
        Map<String, Object> data = new HashMap<>();
        data.put("coins", coins + 20);
        data.put("total_coins_earned", totalEarned + 20);
        data.put(claimedKey, true);
        udm.putMultiple(data);

        animateBalance(coins, coins + 20);
        loadChallenges(); // Refresh all
        showToast("🪙 +20 Coins! Great work!");
    }

    private void setLockedState(TextView btn, String label, int textColor) {
        btn.setText(label);
        btn.setBackgroundResource(R.drawable.bg_pixel_inner_dark);
        btn.setTextColor(textColor);
        btn.setEnabled(false);
    }

    private void setReadyState(TextView btn, String label, int glowColor) {
        btn.setText(label);
        // For ready state, we use a solid color background to make it pop, 
        // or we could use a custom "Ready" drawable. For now, solid color is fine.
        btn.setBackgroundColor(glowColor);
        btn.setTextColor(Color.BLACK);
        btn.setEnabled(true);
    }

    private void setDoneState(TextView btn, int textColor) {
        btn.setText("DONE ✓");
        btn.setBackgroundResource(R.drawable.bg_pixel_inner_dark);
        btn.setTextColor(textColor);
        btn.setEnabled(false);
    }

    private void animateBalance(int from, int to) {
        ValueAnimator va = ValueAnimator.ofInt(from, to);
        va.setDuration(600);
        va.setInterpolator(new AccelerateDecelerateInterpolator());
        va.addUpdateListener(a -> tvBalance.setText(String.valueOf((int) a.getAnimatedValue())));
        va.start();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        udm.syncFromCloud(success -> {
            if (success && !isFinishing()) {
                runOnUiThread(this::loadChallenges);
            }
        });
        loadChallenges();
    }
}
