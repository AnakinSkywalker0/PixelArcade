package com.example.pixelarcade.galaga;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.SoundManager;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class GalagaGameActivity extends AppCompatActivity {

    private TextView tvScore;
    private TextView tvHiScore;
    private TextView tvWave, tvBottomWave;
    private int lives = 3;
    private int currentWave = 1;
    private int currentScore = 0;
    private int hiScore = 0;
    private ImageView[] lifeIcons = new ImageView[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_galaga_game);

        // Immersive full-screen mode
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        androidx.core.view.WindowInsetsControllerCompat controller = 
            new androidx.core.view.WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        tvScore = findViewById(R.id.tvScore);
        tvHiScore = findViewById(R.id.tvHiScore);
        tvWave = findViewById(R.id.tvWave);
        tvBottomWave = findViewById(R.id.tvBottomWave);

        // Load Hi-Score (Separate for Normal and Endless)
        UserDataManager udm = UserDataManager.getInstance(this);
        int mode = getIntent().getIntExtra("LEVEL_NUMBER", 1);
        String hiScoreKey = (mode >= 100) ? "galaga_endless_hi_score" : "galaga_hi_score";
        
        hiScore = udm.getInt(hiScoreKey, 0); 
        tvHiScore.setText(String.format("%05d", hiScore));

        // Increment Plays
        int plays = udm.getInt("galaga_plays", 0);
        udm.putInt("galaga_plays", plays + 1);

        lifeIcons[0] = findViewById(R.id.life1);
        lifeIcons[1] = findViewById(R.id.life2);
        lifeIcons[2] = findViewById(R.id.life3);

        currentWave = getIntent().getIntExtra("LEVEL_NUMBER", 1);
        tvWave.setText(String.format("%02d", currentWave));
        tvBottomWave.setText("WAVE " + String.format("%02d", currentWave));

        GalagaGameView gameView = findViewById(R.id.gameView);
        gameView.setInitialWave(currentWave);
        gameView.setGameListener(new GalagaGameView.GameListener() {
            @Override
            public void onScoreUpdated(int newScore) {
                runOnUiThread(() -> {
                    currentScore = newScore;
                    tvScore.setText(String.format("%05d", newScore));
                    if (newScore > hiScore) {
                        hiScore = newScore;
                        tvHiScore.setText(String.format("%05d", hiScore));
                    }
                });
            }

            @Override
            public void onPlayerHit() {
                runOnUiThread(() -> {
                    lives--;
                    if (lives >= 0 && lives < 3) {
                        lifeIcons[lives].setVisibility(android.view.View.INVISIBLE);
                    }
                    if (lives <= 0) {
                        // Save high score
                        if (currentScore >= hiScore) {
                            int m = getIntent().getIntExtra("LEVEL_NUMBER", 1);
                            String key = (m >= 100) ? "galaga_endless_hi_score" : "galaga_hi_score";
                            UserDataManager.getInstance(GalagaGameActivity.this)
                                .putInt(key, currentScore);
                        }
                        
                        // Trigger the pixel ring death explosion on the canvas first
                        gameView.triggerDeathExplosion();
                        // Delay game over screen so the explosion plays out (~1.5s)
                        new android.os.Handler().postDelayed(() -> {
                            Intent intent = new Intent(GalagaGameActivity.this, GalagaGameOverActivity.class);
                            intent.putExtra("SCORE", currentScore);
                            intent.putExtra("WAVE", currentWave);
                            intent.putExtra("ENEMIES", gameView.getEnemiesDestroyed());
                            intent.putExtra("ACCURACY", gameView.getAccuracy());
                            startActivity(intent);
                            finish();
                        }, 1500);
                    }
                });
            }

            @Override
            public void onLevelComplete() {
                runOnUiThread(() -> {
                    // Save high score
                    if (currentScore >= hiScore) {
                        int m = getIntent().getIntExtra("LEVEL_NUMBER", 1);
                        String key = (m >= 100) ? "galaga_endless_hi_score" : "galaga_hi_score";
                        UserDataManager.getInstance(GalagaGameActivity.this)
                            .putInt(key, currentScore);
                    }
                    
                    int level = getIntent().getIntExtra("LEVEL_NUMBER", 1);
                    UserDataManager udm2 = UserDataManager.getInstance(GalagaGameActivity.this);
                    int unlocked = udm2.getInt("galaga_unlocked_levels", 1);
                    if (level == unlocked && level < 4) {
                        udm2.putInt("galaga_unlocked_levels", level + 1);
                    }
                    Intent intent = new Intent(GalagaGameActivity.this, GalagaLevelsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }
            @Override
            public void onWaveCleared(int nextWave) {
                runOnUiThread(() -> {
                    currentWave = nextWave;
                    tvWave.setText(String.format("%02d", nextWave));
                    tvBottomWave.setText("WAVE " + String.format("%02d", nextWave));
                    
                    int initialMode = getIntent().getIntExtra("LEVEL_NUMBER", 1);
                    if (initialMode >= 100) {
                        UserDataManager udm = UserDataManager.getInstance(GalagaGameActivity.this);
                        int best = udm.getInt("galaga_endless_best_wave", 0);
                        if (nextWave > best) {
                            udm.putInt("galaga_endless_best_wave", nextWave);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundManager.getInstance(this).playBackgroundMusic(R.raw.battle, true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundManager.getInstance(this).pauseBackgroundMusic();
    }
}
