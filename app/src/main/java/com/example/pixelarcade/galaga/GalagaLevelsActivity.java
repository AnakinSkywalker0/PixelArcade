package com.example.pixelarcade.galaga;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.card.MaterialCardView;

public class GalagaLevelsActivity extends AppCompatActivity {

    private int selectedMode = -1; // 1 for Level 1, 100 for Endless
    private MaterialCardView cardLevel1, cardEndless;
    private AppCompatButton btnPlay;
    private TextView tvOverallBest, tvEndlessBest, tvEndlessHiScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_galaga_levels);

        LinearLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        
        AppCompatButton btnMainMenu = findViewById(R.id.btnMainMenu);
        btnMainMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });

        btnPlay = findViewById(R.id.btnPlayLevel);
        btnPlay.setOnClickListener(v -> {
            if (selectedMode != -1) {
                Intent intent = new Intent(this, GalagaWaveBeginActivity.class);
                intent.putExtra("LEVEL_NUMBER", selectedMode);
                startActivity(intent);
            }
        });

        cardLevel1 = findViewById(R.id.cardLevel1);
        cardEndless = findViewById(R.id.cardEndless);
        tvOverallBest = findViewById(R.id.tvOverallBest);
        tvEndlessBest = findViewById(R.id.tvEndlessBest);
        tvEndlessHiScore = findViewById(R.id.tvEndlessHiScore);

        cardLevel1.setOnClickListener(v -> selectMode(1));
        cardEndless.setOnClickListener(v -> selectMode(100)); // 100 as convention for endless
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserDataManager udm = UserDataManager.getInstance(this);
        
        int overallBest = udm.getInt("galaga_hi_score", 0);
        int endlessBestWave = udm.getInt("galaga_endless_best_wave", 0);
        int endlessHiScore = udm.getInt("galaga_endless_hi_score", 0);
        
        tvOverallBest.setText(String.format("%06d", overallBest));
        tvEndlessBest.setText("BEST WAVE " + endlessBestWave);
        tvEndlessHiScore.setText("BEST " + String.format("%05d", endlessHiScore));
    }

    private void selectMode(int mode) {
        selectedMode = mode;
        
        int defaultStroke = (int) (getResources().getDisplayMetrics().density * 1);
        int activeStroke = (int) (getResources().getDisplayMetrics().density * 2);

        // Reset cards
        cardLevel1.setStrokeColor(Color.parseColor("#222222"));
        cardLevel1.setStrokeWidth(defaultStroke);
        cardEndless.setStrokeColor(Color.parseColor("#FF3333"));
        cardEndless.setStrokeWidth(defaultStroke);
        
        // Highlight selected
        if (mode == 1) {
            cardLevel1.setStrokeColor(Color.parseColor("#FFFF00"));
            cardLevel1.setStrokeWidth(activeStroke);
        } else {
            cardEndless.setStrokeColor(Color.parseColor("#FFFF00"));
            cardEndless.setStrokeWidth(activeStroke);
        }

        btnPlay.setEnabled(true);
        btnPlay.setAlpha(1.0f);
    }
}
