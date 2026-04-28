package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.auth.LoginActivity;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private final String[] dots = {".", "..", "..."};
    private int dotIndex = 0;
    private final Handler handler = new Handler(android.os.Looper.getMainLooper());

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

            // Skip auth if user is already logged in
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            }
            finish();
        }, 2500);
    }
}
