package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.auth.AuthActivity;
import com.example.pixelarcade.auth.LoginActivity;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {
    private final android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        android.widget.ProgressBar progressBar = findViewById(R.id.splashProgress);
        
        // Animate the loading bar for a premium feel
        android.animation.ObjectAnimator animation = android.animation.ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration(2200);
        animation.setInterpolator(new android.view.animation.DecelerateInterpolator());
        animation.start();

        handler.postDelayed(() -> {
            // Skip auth if user is already logged in
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            }
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2500);
    }
}
