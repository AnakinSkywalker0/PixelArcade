package com.example.pixelarcade;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private static final int DEFAULT_GRID_SIZE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvProfileLink = findViewById(R.id.tvProfileLink);
        TextView tvLeaderboardLink = findViewById(R.id.tvLeaderboardLink);
        View coinBadge = findViewById(R.id.coinBadge);
        Button btnPlay2048 = findViewById(R.id.btnPlay2048);
        Button btnPlaySpace = findViewById(R.id.btnPlaySpace);
        Button btnPlayTicTacToe = findViewById(R.id.btnPlayTicTacToe);

        tvProfileLink.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        tvLeaderboardLink.setOnClickListener(v -> startActivity(new Intent(this, LeaderboardActivity.class)));
        coinBadge.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));

        btnPlay2048.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameLauncher2048Activity.class);
            startActivity(intent);
        });

        btnPlaySpace.setOnClickListener(v ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show());

        btnPlayTicTacToe.setOnClickListener(v ->
                Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show());
    }
}