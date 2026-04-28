package com.example.pixelarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.content.res.ResourcesCompat;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

public class GameActivity extends AppCompatActivity {

    private GameEngine engine;
    private GridLayout gridLayout;
    private FrameLayout tileContainer;
    private TextView tvCurrentScore;
    private TextView tvHighestScore;
    private int gridSize;
    private int highestScore = 0;

    // Tracking tiles on screen: Map position to its View
    private TextView[][] activeTiles;
    private int cellWidth, cellHeight;

    // Overlay Views
    private View overlayView;
    private TextView tvOverlayTitle;
    private TextView tvOverlaySubtitle;
    private TextView tvOverlayScore;
    private Button btnOverlayAction;
    private Button btnOverlayMenu;
    private Button btnOverlayUndo;
    private boolean hasShownWinDialog = false;
    private boolean hasAwarded2048 = false;
    private boolean hasAwardedNewHigh = false;
    private boolean animationsEnabled = true;
    private boolean gridLinesEnabled = true;

    // Coin Reward Popup
    private LinearLayout coinRewardPopup;
    private TextView tvCoinRewardText;
    private Handler coinHandler = new Handler(new Handler().getLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game);
        
        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        gridSize = getIntent().getIntExtra("GRID_SIZE", 5);
        engine = new GameEngine(gridSize);
        activeTiles = new TextView[gridSize][gridSize];

        gridLayout = findViewById(R.id.gridLayout);
        tileContainer = findViewById(R.id.tileContainer);
        tvCurrentScore = findViewById(R.id.tvCurrentScore);
        tvHighestScore = findViewById(R.id.tvHighestScore);
        Button btnMenu = findViewById(R.id.btnMenu);
        Button btnRestart = findViewById(R.id.btnRestart);

        overlayView = findViewById(R.id.overlayView);
        tvOverlayTitle = findViewById(R.id.tvOverlayTitle);
        tvOverlaySubtitle = findViewById(R.id.tvOverlaySubtitle);
        tvOverlayScore = findViewById(R.id.tvOverlayScore);
        btnOverlayAction = findViewById(R.id.btnOverlayAction);
        btnOverlayMenu = findViewById(R.id.btnOverlayMenu);
        btnOverlayUndo = findViewById(R.id.btnOverlayUndo);

        coinRewardPopup = findViewById(R.id.coinRewardPopup);
        tvCoinRewardText = findViewById(R.id.tvCoinRewardText);

        btnMenu.setOnClickListener(v -> finish());
        btnOverlayMenu.setOnClickListener(v -> finish());
        
        // High Score Persistence
        android.content.SharedPreferences prefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);
        highestScore = prefs.getInt("high_score_2048", 0);
        tvHighestScore.setText(String.valueOf(highestScore));

        // Read settings
        animationsEnabled = prefs.getBoolean("animations", true);
        gridLinesEnabled = prefs.getBoolean("grid_lines", true);

        // Increment Play Count
        int plays = prefs.getInt("plays_2048", 0);
        prefs.edit().putInt("plays_2048", plays + 1).apply();

        btnRestart.setOnClickListener(v -> {
            engine.resetBoard();
            tileContainer.removeAllViews();
            activeTiles = new TextView[gridSize][gridSize];
            hasShownWinDialog = false;
            initialUI();
        });

        setupBackgroundGrid();

        // Wait for layout to calculate cell dimensions
        gridLayout.post(() -> {
            measureCells();
            initialUI();
        });

        findViewById(R.id.main).setOnTouchListener(new OnSwipeTouchListener(this) {
            @Override public void onSwipeLeft() { 
                SoundManager.getInstance(GameActivity.this).playSfx("swipe");
                handleMove(engine.moveLeft()); 
            }
            @Override public void onSwipeRight() { 
                SoundManager.getInstance(GameActivity.this).playSfx("swipe");
                handleMove(engine.moveRight()); 
            }
            @Override public void onSwipeTop() { 
                SoundManager.getInstance(GameActivity.this).playSfx("swipe");
                handleMove(engine.moveUp()); 
            }
            @Override public void onSwipeBottom() { 
                SoundManager.getInstance(GameActivity.this).playSfx("swipe");
                handleMove(engine.moveDown()); 
            }
        });
    }

    private void setupBackgroundGrid() {
        gridLayout.setRowCount(gridSize);
        gridLayout.setColumnCount(gridSize);
        int margin = getResources().getDimensionPixelSize(R.dimen.grid_margin);

        for (int i = 0; i < gridSize * gridSize; i++) {
            View emptyCell = new View(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                    GridLayout.spec(i / gridSize, 1f),
                    GridLayout.spec(i % gridSize, 1f)
            );
            params.width = 0; params.height = 0;
            params.setMargins(margin, margin, margin, margin);
            emptyCell.setLayoutParams(params);
            emptyCell.setBackgroundResource(R.drawable.bg_cell_empty);
            if (!gridLinesEnabled) {
                emptyCell.setAlpha(0f);
            }
            gridLayout.addView(emptyCell);
        }
    }

    private void measureCells() {
        if (gridLayout.getChildCount() > 0) {
            View firstCell = gridLayout.getChildAt(0);
            cellWidth = firstCell.getWidth();
            cellHeight = firstCell.getHeight();
        }
    }

    private void initialUI() {
        int[][] board = engine.getBoard();
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                if (board[i][j] != 0) {
                    addTileAt(i, j, board[i][j]);
                }
            }
        }
        updateScoreOnly();
    }

    private void addTileAt(int row, int col, int value) {
        TextView tile = createTileView(value);
        placeTile(tile, row, col);
        tileContainer.addView(tile);
        activeTiles[row][col] = tile;
    }

    private TextView createTileView(int value) {
        TextView tile = new TextView(this);
        int margin = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cellWidth, cellHeight);
        tile.setLayoutParams(params);
        tile.setGravity(Gravity.CENTER);
        tile.setTextSize(gridSize > 5 ? 18 : 24);
        tile.setTypeface(ResourcesCompat.getFont(this, R.font.press_start_2p));
        tile.setBackgroundResource(R.drawable.bg_cell_empty);
        updateTileStyle(tile, value);
        return tile;
    }

    private void updateTileStyle(TextView tile, int value) {
        String testText = String.valueOf(value);
        tile.setText(testText);
        
        // Auto-scale font size to prevent overlapping boundaries for big numbers
        int len = testText.length();
        int baseSize = gridSize > 5 ? 18 : 24;
        
        if (len == 3) {
            tile.setTextSize(baseSize * 0.75f);
        } else if (len >= 4) {
            tile.setTextSize(baseSize * 0.60f); // 1024, 2048, etc.
        } else {
            tile.setTextSize(baseSize);
        }

        int bgColor = ContextCompat.getColor(this, getTileColor(value));
        int textColor = (value <= 4) ? 
            ContextCompat.getColor(this, R.color.retro_text_dark) : 
            ContextCompat.getColor(this, R.color.retro_text_white);
        tile.getBackground().mutate().setTint(bgColor);
        tile.setTextColor(textColor);
    }

    private void placeTile(View tile, int row, int col) {
        int margin = getResources().getDimensionPixelSize(R.dimen.grid_margin);
        tile.setX(col * (cellWidth + 2 * margin) + margin);
        tile.setY(row * (cellHeight + 2 * margin) + margin);
    }

    private void handleMove(List<GameEngine.MoveEvent> moves) {
        if (moves.isEmpty() || overlayView.getVisibility() == View.VISIBLE) return;

        List<TextView> tilesToRemove = new ArrayList<>();
        TextView[][] nextState = new TextView[gridSize][gridSize];
        
        List<GameEngine.MoveEvent> moveAnimations = new ArrayList<>();
        for (GameEngine.MoveEvent event : moves) {
            if (!event.isNew) moveAnimations.add(event);
        }

        if (moveAnimations.isEmpty()) {
            finalizeMove(moves);
            return;
        }

        final int[] remainingAnimations = {moveAnimations.size()};

        for (GameEngine.MoveEvent event : moveAnimations) {
            TextView tile = activeTiles[event.fromRow][event.fromCol];
            if (tile == null) {
                remainingAnimations[0]--;
                if (remainingAnimations[0] == 0) finalizeMove(moves);
                continue;
            }

            int margin = getResources().getDimensionPixelSize(R.dimen.grid_margin);
            float targetX = event.toCol * (cellWidth + 2 * margin) + margin;
            float targetY = event.toRow * (cellHeight + 2 * margin) + margin;

            // Use hardware layer for smooth translation
            tile.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            
            if (animationsEnabled) {
                tile.animate()
                    .translationX(targetX)
                    .translationY(targetY)
                    .setDuration(140)
                    .setInterpolator(new DecelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            tile.setLayerType(View.LAYER_TYPE_NONE, null);
                            remainingAnimations[0]--;
                            if (remainingAnimations[0] <= 0) {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    for (TextView t : tilesToRemove) tileContainer.removeView(t);
                                    finalizeMove(moves);
                                });
                            }
                        }
                    })
                    .start();
            } else {
                // Instant move without animation
                tile.setTranslationX(targetX);
                tile.setTranslationY(targetY);
                tile.setLayerType(View.LAYER_TYPE_NONE, null);
                remainingAnimations[0]--;
                if (remainingAnimations[0] <= 0) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        for (TextView t : tilesToRemove) tileContainer.removeView(t);
                        finalizeMove(moves);
                    });
                }
            }

            if (event.isMerge) {
                tilesToRemove.add(tile);
            } else {
                nextState[event.toRow][event.toCol] = tile;
            }
        }

        activeTiles = nextState;
    }

    private void finalizeMove(List<GameEngine.MoveEvent> moves) {
        int[][] board = engine.getBoard();
        for (GameEngine.MoveEvent event : moves) {
            if (event.isMerge) {
                // Engine emits 2 merge events per cell; only create tile once
                if (activeTiles[event.toRow][event.toCol] == null) {
                    TextView mergedTile = createTileView(board[event.toRow][event.toCol]);
                    placeTile(mergedTile, event.toRow, event.toCol);
                    tileContainer.addView(mergedTile);
                    activeTiles[event.toRow][event.toCol] = mergedTile;
                    
                    // Juicy pop animation
                    if (animationsEnabled) {
                        mergedTile.setScaleX(0.7f); mergedTile.setScaleY(0.7f);
                        mergedTile.animate()
                            .scaleX(1.0f).scaleY(1.0f)
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator(1.5f))
                            .start();
                    }
                    
                    SoundManager.getInstance(this).playSfx("merge");
                }
            } else if (event.isNew) {
                addTileAt(event.toRow, event.toCol, event.value);
                TextView newTile = activeTiles[event.toRow][event.toCol];
                if (animationsEnabled) {
                    newTile.setAlpha(0f);
                    newTile.setScaleX(0.6f); newTile.setScaleY(0.6f);
                    newTile.animate()
                        .alpha(1f).scaleX(1.0f).scaleY(1.0f)
                        .setDuration(160)
                        .setInterpolator(new OvershootInterpolator(1.2f))
                        .start();
                }
            }
        }
        updateScoreOnly();
        checkStatus();
    }

    private void updateScoreOnly() {
        int score = engine.getScore();
        tvCurrentScore.setText(String.valueOf(score));
        if (score > highestScore) {
            highestScore = score;
            tvHighestScore.setText(String.valueOf(highestScore));
            
            // Save new high score
            getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE)
                .edit().putInt("high_score_2048", highestScore).apply();

            // Award 20 coins for new high score (once per game)
            if (!hasAwardedNewHigh) {
                hasAwardedNewHigh = true;
                awardCoins(20, "🎯 NEW HIGH! +20 🪙");
            }
        }
    }

    private void checkStatus() {
        // Award 100 coins for reaching 2048 (once per game)
        if (engine.hasWon() && !hasAwarded2048) {
            hasAwarded2048 = true;
            awardCoins(100, "🏆 +100 COINS!");
        }

        // Daily Challenge: Reach 512
        if (engine.hasTile(512)) {
            SharedPreferences challengePrefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);
            if (!challengePrefs.getBoolean("challenge_512_done", false)) {
                challengePrefs.edit().putBoolean("challenge_512_done", true).apply();
            }
        }

        if (engine.hasWon() && !hasShownWinDialog) showEndGameOverlay(true);
        else if (engine.isGameOver()) showEndGameOverlay(false);
    }

    private void showEndGameOverlay(boolean isWin) {
        overlayView.setVisibility(View.VISIBLE);
        tvOverlayScore.setText("SCORE: " + engine.getScore());
        btnOverlayUndo.setVisibility(View.GONE);

        if (isWin) {
            tvOverlayTitle.setText("YOU WIN!");
            tvOverlaySubtitle.setVisibility(View.GONE);
            btnOverlayAction.setText("KEEP PLAYING");
            btnOverlayAction.setOnClickListener(v -> {
                hasShownWinDialog = true;
                overlayView.setVisibility(View.GONE);
            });
        } else {
            tvOverlayTitle.setText("GAME OVER");
            tvOverlaySubtitle.setVisibility(View.VISIBLE);
            btnOverlayAction.setText("TRY AGAIN");

            // Show UNDO button if player has enough coins
            SharedPreferences prefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);
            int coins = prefs.getInt("coins", 0);
            if (coins >= 20 && engine.canUndo()) {
                btnOverlayUndo.setVisibility(View.VISIBLE);
                btnOverlayUndo.setOnClickListener(v -> {
                    int currentCoins = prefs.getInt("coins", 0);
                    if (currentCoins >= 20) {
                        prefs.edit().putInt("coins", currentCoins - 20).apply();
                        engine.undo();
                        tileContainer.removeAllViews();
                        activeTiles = new TextView[gridSize][gridSize];
                        overlayView.setVisibility(View.GONE);
                        initialUI();
                        SoundManager.getInstance(this).playSfx("merge");
                    }
                });
            }

            btnOverlayAction.setOnClickListener(v -> {
                engine.resetBoard();
                tileContainer.removeAllViews();
                activeTiles = new TextView[gridSize][gridSize];
                hasShownWinDialog = false;
                hasAwarded2048 = false;
                hasAwardedNewHigh = false;
                overlayView.setVisibility(View.GONE);
                initialUI();
            });
        }
    }

    private void awardCoins(int amount, String message) {
        SharedPreferences prefs = getSharedPreferences("PixelArcadePrefs", MODE_PRIVATE);
        int coins = prefs.getInt("coins", 0);
        int totalEarned = prefs.getInt("total_coins_earned", 0);
        int earned2048 = prefs.getInt("coins_earned_2048", 0);
        prefs.edit()
            .putInt("coins", coins + amount)
            .putInt("total_coins_earned", totalEarned + amount)
            .putInt("coins_earned_2048", earned2048 + amount)
            .apply();
        showCoinReward(message);
    }

    private void showCoinReward(String message) {
        if (coinRewardPopup == null) return;
        tvCoinRewardText.setText(message);
        coinRewardPopup.setVisibility(View.VISIBLE);
        coinRewardPopup.setAlpha(0f);
        coinRewardPopup.setTranslationY(-50f);
        coinRewardPopup.animate()
            .alpha(1f).translationY(0f)
            .setDuration(400)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .withEndAction(() -> coinRewardPopup.animate()
                .alpha(0f).translationY(-30f)
                .setStartDelay(1800)
                .setDuration(300)
                .withEndAction(() -> coinRewardPopup.setVisibility(View.GONE))
                .start())
            .start();
        SoundManager.getInstance(this).playSfx("merge");
    }

    private int getTileColor(int value) {
        switch (value) {
            case 2: return R.color.retro_cell_2;
            case 4: return R.color.retro_cell_4;
            case 8: return R.color.retro_cell_8;
            case 16: return R.color.retro_cell_16;
            case 32: return R.color.retro_cell_32;
            case 64: return R.color.retro_cell_64;
            case 128: return R.color.retro_cell_128;
            case 256: return R.color.retro_cell_256;
            case 512: return R.color.retro_cell_512;
            case 1024: return R.color.retro_cell_1024;
            case 2048: return R.color.retro_cell_2048;
            default: return R.color.retro_cell_empty;
        }
    }
}
