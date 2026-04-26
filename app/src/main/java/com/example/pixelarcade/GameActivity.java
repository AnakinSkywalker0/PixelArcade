package com.example.pixelarcade;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
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
    private boolean hasShownWinDialog = false;

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

        btnMenu.setOnClickListener(v -> finish());
        btnOverlayMenu.setOnClickListener(v -> finish());
        
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
            @Override public void onSwipeLeft() { handleMove(engine.moveLeft()); }
            @Override public void onSwipeRight() { handleMove(engine.moveRight()); }
            @Override public void onSwipeTop() { handleMove(engine.moveUp()); }
            @Override public void onSwipeBottom() { handleMove(engine.moveDown()); }
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
                            // Delay removal slightly to ensure visual consistency
                            new Handler(Looper.getMainLooper()).post(() -> {
                                for (TextView t : tilesToRemove) tileContainer.removeView(t);
                                finalizeMove(moves);
                            });
                        }
                    }
                })
                .start();

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
                    mergedTile.setScaleX(0.7f); mergedTile.setScaleY(0.7f);
                    mergedTile.animate()
                        .scaleX(1.0f).scaleY(1.0f)
                        .setDuration(150)
                        .setInterpolator(new OvershootInterpolator(1.5f))
                        .start();
                }
            } else if (event.isNew) {
                addTileAt(event.toRow, event.toCol, event.value);
                TextView newTile = activeTiles[event.toRow][event.toCol];
                newTile.setAlpha(0f);
                newTile.setScaleX(0.6f); newTile.setScaleY(0.6f);
                newTile.animate()
                    .alpha(1f).scaleX(1.0f).scaleY(1.0f)
                    .setDuration(160)
                    .setInterpolator(new OvershootInterpolator(1.2f))
                    .start();
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
        }
    }

    private void checkStatus() {
        if (engine.hasWon() && !hasShownWinDialog) showEndGameOverlay(true);
        else if (engine.isGameOver()) showEndGameOverlay(false);
    }

    private void showEndGameOverlay(boolean isWin) {
        overlayView.setVisibility(View.VISIBLE);
        tvOverlayScore.setText("SCORE: " + engine.getScore());
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
            btnOverlayAction.setOnClickListener(v -> {
                engine.resetBoard();
                tileContainer.removeAllViews();
                activeTiles = new TextView[gridSize][gridSize];
                hasShownWinDialog = false;
                overlayView.setVisibility(View.GONE);
                initialUI();
            });
        }
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
