package com.example.pixelarcade.galaga;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class GalagaGameOverActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_galaga_game_over);

        // Immersive mode
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        androidx.core.view.WindowInsetsControllerCompat controller = 
            new androidx.core.view.WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // Get stats from Intent
        int score = getIntent().getIntExtra("SCORE", 0);
        int wave = getIntent().getIntExtra("WAVE", 1);
        int enemies = getIntent().getIntExtra("ENEMIES", 0);
        int accuracy = getIntent().getIntExtra("ACCURACY", 0);

        // Get Best Score from DB
        UserDataManager udm = UserDataManager.getInstance(this);
        int best = udm.getInt("galaga_hi_score", 0);
        int bestEndless = udm.getInt("galaga_endless_hi_score", 0);
        int absoluteBest = Math.max(best, bestEndless);

        TextView tvFinalScore = findViewById(R.id.tvFinalScore);
        TextView tvYourBest = findViewById(R.id.tvYourBest);
        TextView tvStatWave = findViewById(R.id.tvStatWave);
        TextView tvStatEnemies = findViewById(R.id.tvStatEnemies);
        TextView tvStatAccuracy = findViewById(R.id.tvStatAccuracy);
        TextView tvNewBest = findViewById(R.id.tvNewBestLabel); // Assuming there's a label for NEW BEST!

        tvFinalScore.setText(String.format("%05d", score));
        tvYourBest.setText(String.format("%05d", absoluteBest));
        tvStatWave.setText(String.format("%02d", wave));
        tvStatEnemies.setText(String.valueOf(enemies));
        tvStatAccuracy.setText(accuracy + "%");

        if (tvNewBest != null) {
            tvNewBest.setVisibility(score >= absoluteBest && score > 0 ? android.view.View.VISIBLE : android.view.View.INVISIBLE);
        }

        findViewById(R.id.btnPlayAgain).setOnClickListener(v -> {
            Intent intent = new Intent(this, GalagaWaveBeginActivity.class);
            intent.putExtra("LEVEL_NUMBER", 1);
            startActivity(intent);
            finish();
        });

        findViewById(R.id.btnMainMenu).setOnClickListener(v -> {
            Intent intent = new Intent(this, GalagaMainMenuActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }
}
