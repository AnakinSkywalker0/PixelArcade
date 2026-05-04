package com.example.pixelarcade.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;
import com.example.pixelarcade.ttt.TicTacToeActivity;
import com.example.pixelarcade.galaga.GalagaMainMenuActivity;
import com.example.pixelarcade.game2048.GameLauncher2048Activity;

public class ShopActivity extends AppCompatActivity {

    private UserDataManager udm;
    private TextView tvCoins;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_shop);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottomNavigation), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom);
            return insets;
        });

        udm = UserDataManager.getInstance(this);
        tvCoins = findViewById(R.id.tvShopCoins);
        updateCoinDisplay();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnClaimGift).setOnClickListener(v -> {
            long lastClaim = udm.getLong("last_free_gift_claim", 0);
            long now = System.currentTimeMillis();
            
            // 24 hour cooldown (86400000 ms)
            if (now - lastClaim > 86400000) {
                int currentCoins = udm.getInt("coins", 0);
                udm.putInt("coins", currentCoins + 50);
                udm.putLong("last_free_gift_claim", now);
                updateCoinDisplay();
                Toast.makeText(this, "Claimed 50 coins!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Gift available in a few hours!", Toast.LENGTH_SHORT).show();
            }
        });

        setupNavigation();
    }

    private void updateCoinDisplay() {
        tvCoins.setText(String.valueOf(udm.getInt("coins", 0)));
    }

    private void setupNavigation() {
        findViewById(R.id.navArcade).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navChallenges).setOnClickListener(v -> {
            startActivity(new Intent(this, DailyChallengesActivity.class));
            finish();
        });
        findViewById(R.id.navHome).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.navSettings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });
    }
}
