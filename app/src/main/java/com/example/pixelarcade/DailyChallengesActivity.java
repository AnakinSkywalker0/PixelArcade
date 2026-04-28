package com.example.pixelarcade;

import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;

public class DailyChallengesActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    // Balance
    private TextView tvBalance;

    // Card 1 — 2048
    private View progress1Fill;
    private TextView tvProgress1;
    private Button btnClaim1;

    // Card 2 — TTT Streak
    private View pip1, pip2, pip3;
    private View progress2Fill; // hidden compat view
    private TextView tvProgress2;
    private Button btnClaim2;

    // Card 3 — Galaga
    private View progress3Fill;
    private TextView tvProgress3;
    private Button btnClaim3;

    // Colors per card (gold, red, teal)
    private static final int COLOR_2048_ACTIVE  = 0xFFFFB300;
    private static final int COLOR_2048_MUTED   = 0xFF3D3000;
    private static final int COLOR_TTT_ACTIVE   = 0xFFFF5252;
    private static final int COLOR_TTT_MUTED    = 0xFF3D0000;
    private static final int COLOR_DONE         = 0xFFFFFFFF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_daily_challenges);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.challengesRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        prefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);

        // Bind views
        tvBalance     = findViewById(R.id.tvChallengeBalance);

        progress1Fill = findViewById(R.id.progress1Fill);
        tvProgress1   = findViewById(R.id.tvProgress1);
        btnClaim1     = findViewById(R.id.btnClaim1);

        pip1          = findViewById(R.id.pip1);
        pip2          = findViewById(R.id.pip2);
        pip3          = findViewById(R.id.pip3);
        progress2Fill = findViewById(R.id.progress2Fill);
        tvProgress2   = findViewById(R.id.tvProgress2);
        btnClaim2     = findViewById(R.id.btnClaim2);

        progress3Fill = findViewById(R.id.progress3Fill);
        tvProgress3   = findViewById(R.id.tvProgress3);
        btnClaim3     = findViewById(R.id.btnClaim3);

        findViewById(R.id.btnChallengesBack).setOnClickListener(v -> finish());

        checkDailyReset();
        loadChallenges();
    }

    // ─────────────────────────────────────────────────────────
    //  Daily reset
    // ─────────────────────────────────────────────────────────
    private void checkDailyReset() {
        long lastReset = prefs.getLong("challenge_last_reset", 0);
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
            prefs.edit()
                .putBoolean("challenge_512_done",           false)
                .putBoolean("challenge_512_claimed",        false)
                .putBoolean("challenge_ttt_streak_done",    false)
                .putBoolean("challenge_ttt_streak_claimed", false)
                .putInt("challenge_ttt_consec_wins",        0)
                .putLong("challenge_last_reset", System.currentTimeMillis())
                .apply();
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Load & render all challenges
    // ─────────────────────────────────────────────────────────
    private void loadChallenges() {
        tvBalance.setText(String.valueOf(prefs.getInt("coins", 0)));
        setup2048Challenge();
        setupTTTChallenge();
    }

    // Card 1 — Reach 512 in 2048
    private void setup2048Challenge() {
        boolean claimed = prefs.getBoolean("challenge_512_claimed", false);
        boolean done    = prefs.getBoolean("challenge_512_done", false);

        if (claimed) {
            animateBar(progress1Fill, 1f, 800);
            tvProgress1.setText("CLAIMED ✓");
            tvProgress1.setTextColor(COLOR_2048_ACTIVE);
            setDoneState(btnClaim1, COLOR_2048_ACTIVE);
        } else if (done) {
            animateBar(progress1Fill, 1f, 800);
            tvProgress1.setText("COMPLETED!");
            tvProgress1.setTextColor(COLOR_2048_ACTIVE);
            setReadyState(btnClaim1, R.drawable.bg_btn_claim, Color.BLACK);
            btnClaim1.setOnClickListener(v -> claim2048());
        } else {
            animateBar(progress1Fill, 0f, 0);
            tvProgress1.setText("NOT STARTED");
            tvProgress1.setTextColor(COLOR_2048_MUTED);
            setLockedState(btnClaim1, "LOCKED", COLOR_2048_MUTED);
        }
    }

    // Card 2 — Win 3 in a row TTT
    private void setupTTTChallenge() {
        boolean claimed = prefs.getBoolean("challenge_ttt_streak_claimed", false);
        boolean done    = prefs.getBoolean("challenge_ttt_streak_done", false);
        int     consec  = prefs.getInt("challenge_ttt_consec_wins", 0);

        if (claimed) {
            fillPip(pip1, true); fillPip(pip2, true); fillPip(pip3, true);
            tvProgress2.setText("CLAIMED ✓");
            tvProgress2.setTextColor(COLOR_TTT_ACTIVE);
            setDoneState(btnClaim2, COLOR_TTT_ACTIVE);
        } else if (done) {
            fillPip(pip1, true); fillPip(pip2, true); fillPip(pip3, true);
            tvProgress2.setText("3/3 — COMPLETED!");
            tvProgress2.setTextColor(COLOR_TTT_ACTIVE);
            setReadyState(btnClaim2, R.drawable.bg_btn_claim_red, Color.WHITE);
            btnClaim2.setOnClickListener(v -> claimTTT());
        } else {
            fillPip(pip1, consec >= 1);
            fillPip(pip2, consec >= 2);
            fillPip(pip3, consec >= 3);
            tvProgress2.setText(consec + " / 3 WINS");
            tvProgress2.setTextColor(COLOR_TTT_MUTED);
            setLockedState(btnClaim2, "LOCKED", COLOR_TTT_MUTED);
        }
    }

    // ─────────────────────────────────────────────────────────
    //  Claim actions
    // ─────────────────────────────────────────────────────────
    private void claim2048() {
        awardCoins("challenge_512_claimed");
        animateBar(progress1Fill, 1f, 600);
        tvProgress1.setText("CLAIMED ✓");
        tvProgress1.setTextColor(COLOR_2048_ACTIVE);
        setDoneState(btnClaim1, COLOR_2048_ACTIVE);
        showToast("🪙 +20 Coins! Keep gaming!");
    }

    private void claimTTT() {
        awardCoins("challenge_ttt_streak_claimed");
        fillPip(pip1, true); fillPip(pip2, true); fillPip(pip3, true);
        tvProgress2.setText("CLAIMED ✓");
        tvProgress2.setTextColor(COLOR_TTT_ACTIVE);
        setDoneState(btnClaim2, COLOR_TTT_ACTIVE);
        showToast("🪙 +20 Coins! Unstoppable streak!");
    }

    private void awardCoins(String claimedKey) {
        int coins       = prefs.getInt("coins", 0);
        int totalEarned = prefs.getInt("total_coins_earned", 0);
        prefs.edit()
            .putInt("coins", coins + 20)
            .putInt("total_coins_earned", totalEarned + 20)
            .putBoolean(claimedKey, true)
            .apply();
        SoundManager.getInstance(this).playSfx("merge");
        // Animate balance counter
        animateBalance(coins, coins + 20);
    }

    // ─────────────────────────────────────────────────────────
    //  UI helpers
    // ─────────────────────────────────────────────────────────
    private void fillPip(View pip, boolean filled) {
        pip.setBackgroundResource(filled
            ? R.drawable.bg_progress_fill_red
            : R.drawable.bg_progress_track);
    }

    private void setLockedState(Button btn, String label, int textColor) {
        btn.setText(label);
        btn.setBackgroundResource(R.drawable.bg_btn_locked);
        btn.setTextColor(textColor);
        btn.setEnabled(false);
    }

    private void setReadyState(Button btn, int bgRes, int textColor) {
        btn.setText("CLAIM NOW");
        btn.setBackgroundResource(bgRes);
        btn.setTextColor(textColor);
        btn.setEnabled(true);
    }

    private void setDoneState(Button btn, int textColor) {
        btn.setText("DONE ✓");
        btn.setBackgroundResource(R.drawable.bg_btn_locked);
        btn.setTextColor(textColor);
        btn.setEnabled(false);
    }

    private void animateBar(View fill, float targetFraction, long delay) {
        fill.post(() -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            View parent = (View) fill.getParent();
            if (parent == null) return;
            int parentW = parent.getWidth();
            int target  = (int)(parentW * Math.max(0, Math.min(targetFraction, 1f)));
            ValueAnimator va = ValueAnimator.ofInt(fill.getWidth(), target);
            va.setDuration(700);
            va.setInterpolator(new AccelerateDecelerateInterpolator());
            va.addUpdateListener(a -> {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) fill.getLayoutParams();
                lp.width = (int) a.getAnimatedValue();
                fill.setLayoutParams(lp);
            });
            va.start();
        }, delay));
    }

    private void animateBalance(int from, int to) {
        ValueAnimator va = ValueAnimator.ofInt(from, to);
        va.setDuration(600);
        va.setInterpolator(new AccelerateDecelerateInterpolator());
        va.addUpdateListener(a ->
            tvBalance.setText(String.valueOf((int) a.getAnimatedValue())));
        va.start();
    }

    private void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    // ─────────────────────────────────────────────────────────
    @Override
    protected void onResume() {
        super.onResume();
        loadChallenges();
    }
}
