package com.example.pixelarcade.main;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.SoundManager;
import com.example.pixelarcade.manager.UserDataManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvPlayerName, tvSubTitle, tvMemberSince, tvProfileAvatarEmoji;
    private ImageView ivProfileImage;
    private TextView tvTotalCoinsEarned, tvHighScore, tvTotalGames;
    private TextView tv2048Plays, tv2048High, tv2048Coins;
    private TextView tvTttPlays, tvTttWins, tvTttWinRate;
    private TextView tvGalagaPlays, tvGalagaHigh, tvGalagaWave;
    private UserDataManager udm;

    private Uri cameraImageUri;

    // Multiple permissions launcher (camera + storage)
    private final ActivityResultLauncher<String[]> requestMultiPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), results -> {
                boolean cameraGranted = Boolean.TRUE.equals(results.get(Manifest.permission.CAMERA));
                if (cameraGranted) {
                    showImageSourceDialog();
                } else {
                    Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success) {
                    handleImageResult(cameraImageUri);
                }
            });

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    handleImageResult(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileRoot), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        udm = UserDataManager.getInstance(this);

        // Initialize Views
        tvPlayerName = findViewById(R.id.tvPlayerName);
        tvSubTitle = findViewById(R.id.tvSubTitle);
        tvMemberSince = findViewById(R.id.tvMemberSince);
        tvProfileAvatarEmoji = findViewById(R.id.tvProfileAvatarEmoji);
        ivProfileImage = findViewById(R.id.ivProfileImage);

        // Top stats row
        tvTotalCoinsEarned = findViewById(R.id.tvTotalCoinsEarned);
        tvHighScore = findViewById(R.id.tvHighScore);
        tvTotalGames = findViewById(R.id.tvTotalGames);

        // 2048 breakdown
        tv2048Plays = findViewById(R.id.tv2048Plays);
        tv2048High = findViewById(R.id.tv2048High);
        tv2048Coins = findViewById(R.id.tv2048Coins);

        // TTT breakdown
        tvTttPlays = findViewById(R.id.tvTttPlays);
        tvTttWins = findViewById(R.id.tvTttWins);
        tvTttWinRate = findViewById(R.id.tvTttWinRate);

        // Galaga breakdown
        tvGalagaPlays = findViewById(R.id.tvGalagaPlays);
        tvGalagaHigh = findViewById(R.id.tvGalagaHigh);
        tvGalagaWave = findViewById(R.id.tvGalagaWave);
        
        // Back button
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Photo feature
        findViewById(R.id.btnEditPhoto).setOnClickListener(v -> checkPermissionsAndShowDialog());

        // Action buttons
        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));

        findViewById(R.id.btnSocial).setOnClickListener(v ->
                Toast.makeText(this, "Social features coming soon!", Toast.LENGTH_SHORT).show());

        findViewById(R.id.btnViewLeaderboard).setOnClickListener(v ->
                startActivity(new Intent(this, LeaderboardActivity.class)));

        loadProfile();
    }

    private void checkPermissionsAndShowDialog() {
        boolean cameraOk = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;

        if (cameraOk) {
            showImageSourceDialog();
        } else {
            // Request camera (gallery uses GetContent which handles its own permission on modern Android)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestMultiPermissionLauncher.launch(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_IMAGES
                });
            } else {
                requestMultiPermissionLauncher.launch(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                });
            }
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Remove / Default"};
        new AlertDialog.Builder(this)
                .setTitle("Update Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else if (which == 1) {
                        pickImageLauncher.launch("image/*");
                    } else {
                        removeCustomPhoto();
                    }
                })
                .show();
    }

    private void launchCamera() {
        try {
            File photoFile = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photoFile);
            takePictureLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile("PROFILE_" + timeStamp, ".jpg", storageDir);
    }

    private void handleImageResult(Uri uri) {
        try {
            Bitmap bitmap = getCorrectlyOrientedBitmap(uri);
            if (bitmap != null) {
                String savedPath = saveToInternalStorage(bitmap);
                udm.putString("custom_profile_image", savedPath);
                loadProfile();
                Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getCorrectlyOrientedBitmap(Uri uri) throws IOException {
        InputStream is = getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        is.close();

        // Fix rotation
        InputStream input = getContentResolver().openInputStream(uri);
        ExifInterface exif = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            exif = new ExifInterface(input);
        }
        int orientation = exif != null ? exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL) : ExifInterface.ORIENTATION_NORMAL;
        input.close();

        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90: matrix.postRotate(90); break;
            case ExifInterface.ORIENTATION_ROTATE_180: matrix.postRotate(180); break;
            case ExifInterface.ORIENTATION_ROTATE_270: matrix.postRotate(270); break;
        }

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    private String saveToInternalStorage(Bitmap bitmap) {
        File directory = new File(getFilesDir(), "profile_photos");
        if (!directory.exists()) directory.mkdirs();
        File file = new File(directory, "current_profile.jpg");
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void removeCustomPhoto() {
        udm.remove("custom_profile_image");
        loadProfile();
    }

    private void loadProfile() {
        // Player name
        String name = udm.getString("playerName", "BUDDY");
        tvPlayerName.setText(name);

        // Player Tagline
        String tagline = udm.getString("playerTagline", null);
        if (tagline == null || tagline.isEmpty()) {
            int totalGames = udm.getInt("plays_2048", 0) + udm.getInt("plays_ttt", 0) + udm.getInt("galaga_plays", 0);
            if (totalGames >= 100) tagline = "ARCADE LEGEND";
            else if (totalGames >= 50) tagline = "ARCADE MASTER";
            else if (totalGames >= 20) tagline = "ARCADE WARRIOR";
            else if (totalGames >= 5) tagline = "ARCADE ROOKIE";
            else tagline = "NEW PLAYER";
        }
        tvSubTitle.setText(tagline);

        // Avatar / Image
        String customImagePath = udm.getString("custom_profile_image", null);
        if (customImagePath != null && new File(customImagePath).exists()) {
            tvProfileAvatarEmoji.setVisibility(View.GONE);
            ivProfileImage.setVisibility(View.VISIBLE);
            // Force reload by clearing cache
            ivProfileImage.setImageURI(null);
            ivProfileImage.setImageURI(Uri.fromFile(new File(customImagePath)));
        } else {
            tvProfileAvatarEmoji.setVisibility(View.VISIBLE);
            ivProfileImage.setVisibility(View.GONE);
            tvProfileAvatarEmoji.setText(udm.getString("playerAvatarEmoji", "👾"));
        }

        // Member since
        String joinDate = udm.getString("join_date", null);
        if (joinDate == null) {
            joinDate = new SimpleDateFormat("MMM yyyy", Locale.US).format(new Date());
            udm.putString("join_date", joinDate);
        }
        tvMemberSince.setText("MEMBER SINCE " + joinDate.toUpperCase());

        // Top Stats
        int totalEarned = udm.getInt("total_coins_earned", 0);
        tvTotalCoinsEarned.setText(formatNumber(totalEarned));

        int high2048 = udm.getInt("high_score_2048", 0);
        int highGalaga = udm.getInt("galaga_hi_score", 0);
        int highEndless = udm.getInt("galaga_endless_hi_score", 0);
        int overallBest = Math.max(high2048, Math.max(highGalaga, highEndless));
        tvHighScore.setText(formatNumber(overallBest));

        int totalGamesCount = udm.getInt("plays_2048", 0) + udm.getInt("plays_ttt", 0) + udm.getInt("galaga_plays", 0);
        tvTotalGames.setText(String.valueOf(totalGamesCount));

        // 2048 Breakdown
        int plays2048 = udm.getInt("plays_2048", 0);
        int coins2048 = udm.getInt("coins_earned_2048", 0);
        tv2048Plays.setText(String.valueOf(plays2048));
        tv2048High.setText(formatNumber(high2048));
        tv2048Coins.setText(String.valueOf(coins2048));

        // TTT Breakdown
        int playsTtt = udm.getInt("plays_ttt", 0);
        int winsTtt = udm.getInt("wins_ttt", 0);
        int winRate = playsTtt > 0 ? (winsTtt * 100 / playsTtt) : 0;
        tvTttPlays.setText(String.valueOf(playsTtt));
        tvTttWins.setText(String.valueOf(winsTtt));
        tvTttWinRate.setText(winRate + "%");

        // Galaga Breakdown
        int playsGalaga = udm.getInt("galaga_plays", 0);
        int waveGalaga = udm.getInt("galaga_endless_best_wave", 0);
        tvGalagaPlays.setText(String.valueOf(playsGalaga));
        tvGalagaHigh.setText(formatNumber(Math.max(highGalaga, highEndless)));
        tvGalagaWave.setText(String.valueOf(waveGalaga));
    }

    private String formatNumber(int num) {
        if (num >= 10000) {
            return String.format(Locale.US, "%.1fK", num / 1000.0);
        }
        return String.valueOf(num);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }
}
