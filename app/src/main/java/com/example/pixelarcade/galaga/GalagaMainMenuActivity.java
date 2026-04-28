package com.example.pixelarcade.galaga;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GalagaMainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_galaga_main_menu);

        // Enable immersive full-screen mode to occupy the whole screen
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        androidx.core.view.WindowInsetsControllerCompat controller = 
            new androidx.core.view.WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        LinearLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        TextView tvPressStart = findViewById(R.id.tvPressStart);
        TextView tvHighScore = findViewById(R.id.tvHighScore);
        
        UserDataManager udm = UserDataManager.getInstance(this);
        int hiScore = udm.getInt("galaga_hi_score", 0);
        tvHighScore.setText(String.format("%06d", hiScore));
        
        AlphaAnimation blinkAnimation = new AlphaAnimation(1.0f, 0.0f);
        blinkAnimation.setDuration(600); // 600ms per blink
        blinkAnimation.setInterpolator(new LinearInterpolator());
        blinkAnimation.setRepeatCount(Animation.INFINITE);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        tvPressStart.startAnimation(blinkAnimation);

        tvPressStart.setOnClickListener(v -> {
            Intent intent = new Intent(this, GalagaTutorialActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundManager.getInstance(this).playBackgroundMusic(R.raw.start_music, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundManager.getInstance(this).pauseBackgroundMusic();
    }
}
