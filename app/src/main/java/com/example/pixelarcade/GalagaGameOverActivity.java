package com.example.pixelarcade;

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

        // Randomize score and stats for now
        Random random = new Random();
        int randomScore = (random.nextInt(30) + 10) * 100;
        int randomWave = random.nextInt(3) + 1;
        int randomEnemies = random.nextInt(50) + 10;
        int randomAccuracy = random.nextInt(40) + 40;

        TextView tvFinalScore = findViewById(R.id.tvFinalScore);
        TextView tvYourBest = findViewById(R.id.tvYourBest);
        TextView tvStatWave = findViewById(R.id.tvStatWave);
        TextView tvStatEnemies = findViewById(R.id.tvStatEnemies);
        TextView tvStatAccuracy = findViewById(R.id.tvStatAccuracy);

        tvFinalScore.setText(String.format("%05d", randomScore));
        tvYourBest.setText(String.format("%05d", randomScore));
        tvStatWave.setText(String.format("%02d", randomWave));
        tvStatEnemies.setText(String.valueOf(randomEnemies));
        tvStatAccuracy.setText(randomAccuracy + "%");

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
