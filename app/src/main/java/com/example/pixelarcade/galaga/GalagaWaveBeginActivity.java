package com.example.pixelarcade.galaga;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class GalagaWaveBeginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_galaga_wave_begin);

        // Immersive full-screen mode
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        androidx.core.view.WindowInsetsControllerCompat controller = 
            new androidx.core.view.WindowInsetsControllerCompat(getWindow(), getWindow().getDecorView());
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        // Retrieve level info
        int level = getIntent().getIntExtra("LEVEL_NUMBER", 1);
        
        TextView tvLevelName = findViewById(R.id.tvLevelName);
        if (level >= 100) {
            tvLevelName.setText("ENDLESS MODE");
        } else {
            tvLevelName.setText("LEVEL " + level);
        }

        // Simple transition: wait 2.5 seconds, then launch the Game!
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, GalagaGameActivity.class);
            intent.putExtra("LEVEL_NUMBER", level);
            startActivity(intent);
            finish(); // Remove this transition screen from the backstack
        }, 2000);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SoundManager.getInstance(this).playBackgroundMusic(R.raw.level_start, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SoundManager.getInstance(this).pauseBackgroundMusic();
    }
}
