package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etTagline;
    private TextView tvCurrentAvatarEmoji;
    private LinearLayout avatarContainer;
    private UserDataManager udm;
    
    private String selectedAvatarEmoji = "👾"; // Default
    private final String[] avatars = {"👾", "🤖", "🚀", "⚔️", "🛡️", "🕹️", "🎮", "🐲"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editProfileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        etName = findViewById(R.id.etName);
        etTagline = findViewById(R.id.etTagline);
        tvCurrentAvatarEmoji = findViewById(R.id.tvCurrentAvatarEmoji);
        avatarContainer = findViewById(R.id.avatarContainer);
        udm = UserDataManager.getInstance(this);

        // Load Current Data
        String currentName = udm.getString("playerName", "BUDDY");
        String currentTagline = udm.getString("playerTagline", "ARCADE WARRIOR");
        selectedAvatarEmoji = udm.getString("playerAvatarEmoji", "👾");
        
        etName.setText(currentName);
        etTagline.setText(currentTagline);
        updateAvatarPreview();

        // Setup Avatar Picker
        setupAvatarPicker();

        // Click Listeners
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveProfile());
    }

    private void setupAvatarPicker() {
        avatarContainer.removeAllViews();
        for (String emoji : avatars) {
            TextView tv = new TextView(this);
            tv.setText(emoji);
            tv.setTextSize(32);
            tv.setGravity(Gravity.CENTER);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(140, 140);
            params.setMargins(10, 0, 10, 0);
            tv.setLayoutParams(params);
            
            updateItemSelection(tv, emoji.equals(selectedAvatarEmoji));

            tv.setOnClickListener(v -> {
                selectedAvatarEmoji = emoji;
                updateAvatarPreview();
                setupAvatarPicker(); // Refresh to update borders
            });

            avatarContainer.addView(tv);
        }
    }

    private void updateItemSelection(TextView tv, boolean isSelected) {
        if (isSelected) {
            tv.setBackgroundResource(R.drawable.bg_avatar_selected);
        } else {
            tv.setBackgroundResource(R.drawable.bg_avatar_unselected);
        }
    }

    private void updateAvatarPreview() {
        tvCurrentAvatarEmoji.setText(selectedAvatarEmoji);
    }

    private void saveProfile() {
        String newName = etName.getText().toString().trim();
        String newTagline = etTagline.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to cloud
        udm.putString("playerName", newName);
        udm.putString("playerTagline", newTagline);
        udm.putString("playerAvatarEmoji", selectedAvatarEmoji);

        SoundManager.getInstance(this).playSfx("merge");
        Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
