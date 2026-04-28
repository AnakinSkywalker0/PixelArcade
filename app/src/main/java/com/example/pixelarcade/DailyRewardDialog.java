package com.example.pixelarcade;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.Calendar;

public class DailyRewardDialog extends Dialog {

    public interface OnRewardClaimedListener {
        void onRewardClaimed(int coins);
    }

    private OnRewardClaimedListener listener;
    private int[] rewards = {10, 20, 30, 40, 50, 60, 100};
    private int currentStreak = 0;
    private boolean isClaimableToday = false;

    public DailyRewardDialog(Context context, OnRewardClaimedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_daily_reward);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        calculateStreak();
        setupUI();
    }

    private void calculateStreak() {
        SharedPreferences prefs = getContext().getSharedPreferences("PixelArcadePrefs", Context.MODE_PRIVATE);
        long lastClaimTime = prefs.getLong("last_daily_claim", 0);
        currentStreak = prefs.getInt("streak_days", 0);

        if (lastClaimTime == 0) {
            isClaimableToday = true;
            currentStreak = 0;
            return;
        }

        Calendar lastClaim = Calendar.getInstance();
        lastClaim.setTimeInMillis(lastClaimTime);
        lastClaim.set(Calendar.HOUR_OF_DAY, 0);
        lastClaim.set(Calendar.MINUTE, 0);
        lastClaim.set(Calendar.SECOND, 0);
        lastClaim.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        long diffMillis = today.getTimeInMillis() - lastClaim.getTimeInMillis();
        long diffDays = diffMillis / (24 * 60 * 60 * 1000);

        if (diffDays == 0) {
            // Already claimed today
            isClaimableToday = false;
        } else if (diffDays == 1) {
            // Streak continues
            isClaimableToday = true;
            if (currentStreak >= 7) {
                currentStreak = 0; // Loop back to day 1
            }
        } else {
            // Streak broken
            isClaimableToday = true;
            currentStreak = 0;
        }
    }

    private void setupUI() {
        LinearLayout[] cards = new LinearLayout[7];
        cards[0] = findViewById(R.id.cardDay1);
        cards[1] = findViewById(R.id.cardDay2);
        cards[2] = findViewById(R.id.cardDay3);
        cards[3] = findViewById(R.id.cardDay4);
        cards[4] = findViewById(R.id.cardDay5);
        cards[5] = findViewById(R.id.cardDay6);
        cards[6] = findViewById(R.id.cardDay7);

        TextView[] titles = new TextView[7];
        titles[0] = findViewById(R.id.tvTitleDay1);
        titles[1] = findViewById(R.id.tvTitleDay2);
        titles[2] = findViewById(R.id.tvTitleDay3);
        titles[3] = findViewById(R.id.tvTitleDay4);
        titles[4] = findViewById(R.id.tvTitleDay5);
        titles[5] = findViewById(R.id.tvTitleDay6);
        titles[6] = findViewById(R.id.tvTitleDay7);

        Button btnAction = findViewById(R.id.btnDailyRewardAction);
        TextView tvSubtitle = findViewById(R.id.tvDailyRewardSubtitle);
        TextView tvClose = findViewById(R.id.tvCloseLater);

        tvClose.setOnClickListener(v -> dismiss());

        // Update card visual states
        for (int i = 0; i < 7; i++) {
            if (i < currentStreak) {
                // Claimed past days
                cards[i].setBackgroundResource(R.drawable.bg_day_card_claimed);
                titles[i].setTextColor(Color.parseColor("#4CAF50")); // Green
                cards[i].setAlpha(0.6f);
            } else if (i == currentStreak && isClaimableToday) {
                // Today's claimable reward
                cards[i].setBackgroundResource(R.drawable.bg_day_card_claimable);
                titles[i].setTextColor(Color.parseColor("#D4AF37")); // Gold
                cards[i].setAlpha(1.0f);
            } else {
                // Locked future days
                cards[i].setBackgroundResource(R.drawable.bg_day_card_locked);
                titles[i].setTextColor(Color.parseColor("#A0A0A0")); // Grey
                cards[i].setAlpha(0.6f);
            }
        }

        if (isClaimableToday) {
            Animation pulse = AnimationUtils.loadAnimation(getContext(), R.anim.pulse);
            cards[currentStreak].startAnimation(pulse);
            btnAction.startAnimation(pulse);

            btnAction.setText("CLAIM DAY " + (currentStreak + 1));
            btnAction.setBackgroundResource(R.drawable.bg_auth_btn_primary);
            btnAction.setTextColor(Color.parseColor("#F3F3F3"));
            btnAction.setOnClickListener(v -> {
                cards[currentStreak].clearAnimation();
                btnAction.clearAnimation();
                claimReward(btnAction, tvSubtitle, cards[currentStreak], titles[currentStreak]);
            });
        } else {
            btnAction.setText("COME BACK TOMORROW");
            btnAction.setBackgroundResource(R.drawable.bg_day_card_locked);
            btnAction.setTextColor(Color.parseColor("#76716C"));
            btnAction.setOnClickListener(null); // Disabled
        }
    }

    private void claimReward(Button btnAction, TextView tvSubtitle, LinearLayout claimedCard, TextView claimedTitle) {
        if (!isClaimableToday) return;

        int wonAmount = rewards[currentStreak];

        // Update Data
        SharedPreferences prefs = getContext().getSharedPreferences("PixelArcadePrefs", Context.MODE_PRIVATE);
        int currentCoins = prefs.getInt("coins", 0);
        int totalEarned = prefs.getInt("total_coins_earned", 0);
        prefs.edit()
             .putInt("coins", currentCoins + wonAmount)
             .putInt("total_coins_earned", totalEarned + wonAmount)
             .putInt("streak_days", currentStreak + 1)
             .putLong("last_daily_claim", System.currentTimeMillis())
             .apply();

        // Play Sound
        SoundManager.getInstance(getContext()).playSfx("merge");

        // Update UI
        isClaimableToday = false;
        claimedCard.setBackgroundResource(R.drawable.bg_day_card_claimed);
        claimedTitle.setTextColor(Color.parseColor("#4CAF50"));
        
        tvSubtitle.setText("You claimed " + wonAmount + " coins!");
        tvSubtitle.setTextColor(Color.parseColor("#4CAF50"));
        
        btnAction.setText("AWESOME!");
        btnAction.setOnClickListener(v -> dismiss());

        if (listener != null) {
            listener.onRewardClaimed(wonAmount);
        }
    }
}
