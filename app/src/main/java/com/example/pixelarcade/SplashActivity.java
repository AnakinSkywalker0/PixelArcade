package com.example.pixelarcade;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    private final String[] dots = {".", "..", "..."};
    private int dotIndex = 0;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvDots = findViewById(R.id.tvDots);

        Runnable dotRunnable = new Runnable() {
            @Override
            public void run() {
                tvDots.setText(dots[dotIndex % 3]);
                dotIndex++;
                handler.postDelayed(this, 400);
            }
        };
        handler.post(dotRunnable);

        handler.postDelayed(() -> {
            handler.removeCallbacksAndMessages(null);
            startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            finish();
        }, 2500);
    }
}

