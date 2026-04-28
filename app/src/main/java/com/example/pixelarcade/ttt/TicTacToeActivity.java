package com.example.pixelarcade.ttt;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.UserDataManager;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToeActivity extends AppCompatActivity {

    // 0 = empty, 1 = player (sword), 2 = cpu (shield)
    private int[][] board = new int[3][3];
    private TextView[][] cellViews = new TextView[3][3];
    private boolean playerTurn = true;
    private boolean gameOver = false;
    private int difficulty = 1; // 0=Easy, 1=Medium, 2=Hard
    private boolean animationsEnabled = true;
    private boolean playerFirst = true;

    private int playerWins = 0;
    private int cpuWins = 0;
    private int draws = 0;

    private TextView tvPlayerScore, tvCpuScore, tvDrawScore, tvTurnIndicator;
    private GridLayout tttGrid;
    private FrameLayout tttOverlay;
    private TextView tvTttOverlayTitle, tvTttOverlaySubtitle;
    private Button btnTttPlayAgain, btnTttOverlayMenu;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    // Coin Reward Popup
    private LinearLayout coinRewardPopup;
    private TextView tvCoinRewardText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tictactoe);

        View root = findViewById(R.id.main);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // Initialize views
        tvPlayerScore = findViewById(R.id.tvPlayerScore);
        tvCpuScore = findViewById(R.id.tvCpuScore);
        tvDrawScore = findViewById(R.id.tvDrawScore);
        tvTurnIndicator = findViewById(R.id.tvTurnIndicator);
        tttGrid = findViewById(R.id.tttGrid);

        tttOverlay = findViewById(R.id.tttOverlay);
        tvTttOverlayTitle = findViewById(R.id.tvTttOverlayTitle);
        tvTttOverlaySubtitle = findViewById(R.id.tvTttOverlaySubtitle);
        btnTttPlayAgain = findViewById(R.id.btnTttPlayAgain);
        btnTttOverlayMenu = findViewById(R.id.btnTttOverlayMenu);

        Button btnMenu = findViewById(R.id.btnTttMenu);
        Button btnRestart = findViewById(R.id.btnTttRestart);

        btnMenu.setOnClickListener(v -> finish());
        btnRestart.setOnClickListener(v -> resetBoard());
        btnTttOverlayMenu.setOnClickListener(v -> finish());
        btnTttPlayAgain.setOnClickListener(v -> {
            tttOverlay.setVisibility(View.GONE);
            resetBoard();
        });

        // Read difficulty from launcher
        difficulty = getIntent().getIntExtra("DIFFICULTY", 1);

        // Coin reward popup
        coinRewardPopup = findViewById(R.id.tttCoinRewardPopup);
        tvCoinRewardText = findViewById(R.id.tvTttCoinRewardText);

        // Read settings
        UserDataManager udm = UserDataManager.getInstance(this);
        animationsEnabled = udm.getBoolean("animations", true);
        playerFirst = udm.getBoolean("ttt_player_first", true);

        // Increment play count
        int plays = udm.getInt("plays_ttt", 0);
        udm.putInt("plays_ttt", plays + 1);

        tttGrid.post(() -> {
            buildGrid();
            // If Player First is OFF, let CPU go first
            if (!playerFirst) {
                playerTurn = false;
                tvTurnIndicator.setText("🛡️ CPU THINKING...");
                tvTurnIndicator.setTextColor(ContextCompat.getColor(this, R.color.ttt_shield_color));
                handler.postDelayed(() -> {
                    cpuMove();
                    if (!checkGameEnd()) {
                        playerTurn = true;
                        tvTurnIndicator.setText("⚔️ YOUR TURN");
                        tvTurnIndicator.setTextColor(ContextCompat.getColor(this, R.color.ttt_sword_color));
                    }
                }, 600);
            }
        });
    }

    private void buildGrid() {
        tttGrid.removeAllViews();
        int gridW = tttGrid.getWidth();
        int gridH = tttGrid.getHeight();
        int margin = 6;
        int cellW = (gridW - margin * 6) / 3;
        int cellH = (gridH - margin * 6) / 3;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                TextView cell = new TextView(this);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(r, 1f),
                        GridLayout.spec(c, 1f)
                );
                params.width = cellW;
                params.height = cellH;
                params.setMargins(margin, margin, margin, margin);
                cell.setLayoutParams(params);
                cell.setGravity(Gravity.CENTER);
                cell.setBackgroundResource(R.drawable.bg_ttt_cell);
                cell.setTextSize(36);
                cell.setTypeface(ResourcesCompat.getFont(this, R.font.press_start_2p));
                cell.setTextColor(Color.TRANSPARENT);

                final int row = r, col = c;
                cell.setOnClickListener(v -> onCellClick(row, col));

                tttGrid.addView(cell);
                cellViews[r][c] = cell;
            }
        }
    }

    private void onCellClick(int row, int col) {
        if (gameOver || !playerTurn || board[row][col] != 0) return;

        // Player places sword
        makeMove(row, col, 1);
        SoundManager.getInstance(this).playSfx("click");

        if (checkGameEnd()) return;

        // CPU turn
        playerTurn = false;
        tvTurnIndicator.setText("🛡️ CPU THINKING...");
        tvTurnIndicator.setTextColor(ContextCompat.getColor(this, R.color.ttt_shield_color));

        handler.postDelayed(() -> {
            cpuMove();
            if (!checkGameEnd()) {
                playerTurn = true;
                tvTurnIndicator.setText("⚔️ YOUR TURN");
                tvTurnIndicator.setTextColor(ContextCompat.getColor(this, R.color.ttt_sword_color));
            }
        }, 600);
    }

    private void makeMove(int row, int col, int player) {
        board[row][col] = player;
        TextView cell = cellViews[row][col];
        if (player == 1) {
            cell.setText("⚔️");
            cell.setTextColor(ContextCompat.getColor(this, R.color.ttt_sword_color));
        } else {
            cell.setText("🛡️");
            cell.setTextColor(ContextCompat.getColor(this, R.color.ttt_shield_color));
        }
        // Pop-in animation (conditional on settings)
        if (animationsEnabled) {
            cell.setScaleX(0f);
            cell.setScaleY(0f);
            cell.setAlpha(0f);
            cell.animate()
                .scaleX(1f).scaleY(1f).alpha(1f)
                .setDuration(250)
                .setInterpolator(new android.view.animation.OvershootInterpolator(1.5f))
                .start();
        }
    }

    // --- AI: Minimax-based CPU ---
    private void cpuMove() {
        int[] bestMove = findBestMove();
        if (bestMove != null) {
            makeMove(bestMove[0], bestMove[1], 2);
            SoundManager.getInstance(this).playSfx("click");
        }
    }

    private int[] findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;

        // Difficulty-based randomness: Easy=60%, Medium=30%, Hard=0%
        float randomChance = (difficulty == 0) ? 0.6f : (difficulty == 1) ? 0.3f : 0f;
        if (random.nextFloat() < randomChance) {
            List<int[]> emptySlots = getEmptyCells();
            if (!emptySlots.isEmpty()) {
                return emptySlots.get(random.nextInt(emptySlots.size()));
            }
        }

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == 0) {
                    board[r][c] = 2;
                    int score = minimax(false, 0);
                    board[r][c] = 0;
                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new int[]{r, c};
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(boolean isMaximizing, int depth) {
        int winner = checkWinner();
        if (winner == 2) return 10 - depth;   // CPU wins
        if (winner == 1) return depth - 10;    // Player wins
        if (isBoardFull()) return 0;           // Draw

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c] == 0) {
                        board[r][c] = 2;
                        bestScore = Math.max(bestScore, minimax(false, depth + 1));
                        board[r][c] = 0;
                    }
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (board[r][c] == 0) {
                        board[r][c] = 1;
                        bestScore = Math.min(bestScore, minimax(true, depth + 1));
                        board[r][c] = 0;
                    }
                }
            }
            return bestScore;
        }
    }

    // --- Win/Draw/Loss checking ---
    private boolean checkGameEnd() {
        int winner = checkWinner();
        if (winner != 0) {
            gameOver = true;
            highlightWinningCells(winner);
            handler.postDelayed(() -> showResult(winner), 800);
            return true;
        }
        if (isBoardFull()) {
            gameOver = true;
            draws++;
            tvDrawScore.setText(String.valueOf(draws));
            handler.postDelayed(() -> showResult(0), 400);
            return true;
        }
        return false;
    }

    private int checkWinner() {
        // Rows
        for (int r = 0; r < 3; r++) {
            if (board[r][0] != 0 && board[r][0] == board[r][1] && board[r][1] == board[r][2])
                return board[r][0];
        }
        // Columns
        for (int c = 0; c < 3; c++) {
            if (board[0][c] != 0 && board[0][c] == board[1][c] && board[1][c] == board[2][c])
                return board[0][c];
        }
        // Diagonals
        if (board[0][0] != 0 && board[0][0] == board[1][1] && board[1][1] == board[2][2])
            return board[0][0];
        if (board[0][2] != 0 && board[0][2] == board[1][1] && board[1][1] == board[2][0])
            return board[0][2];
        return 0;
    }

    private boolean isBoardFull() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board[r][c] == 0) return false;
        return true;
    }

    private List<int[]> getEmptyCells() {
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (board[r][c] == 0) empty.add(new int[]{r, c});
        return empty;
    }

    private void highlightWinningCells(int winner) {
        int gold = ContextCompat.getColor(this, R.color.ttt_win_highlight);
        // Rows
        for (int r = 0; r < 3; r++) {
            if (board[r][0] == winner && board[r][1] == winner && board[r][2] == winner) {
                for (int c = 0; c < 3; c++)
                    cellViews[r][c].setBackgroundColor(Color.parseColor("#3DFFD700"));
            }
        }
        // Columns
        for (int c = 0; c < 3; c++) {
            if (board[0][c] == winner && board[1][c] == winner && board[2][c] == winner) {
                for (int r = 0; r < 3; r++)
                    cellViews[r][c].setBackgroundColor(Color.parseColor("#3DFFD700"));
            }
        }
        // Diagonals
        if (board[0][0] == winner && board[1][1] == winner && board[2][2] == winner) {
            cellViews[0][0].setBackgroundColor(Color.parseColor("#3DFFD700"));
            cellViews[1][1].setBackgroundColor(Color.parseColor("#3DFFD700"));
            cellViews[2][2].setBackgroundColor(Color.parseColor("#3DFFD700"));
        }
        if (board[0][2] == winner && board[1][1] == winner && board[2][0] == winner) {
            cellViews[0][2].setBackgroundColor(Color.parseColor("#3DFFD700"));
            cellViews[1][1].setBackgroundColor(Color.parseColor("#3DFFD700"));
            cellViews[2][0].setBackgroundColor(Color.parseColor("#3DFFD700"));
        }
    }

    private void showResult(int winner) {
        tttOverlay.setVisibility(View.VISIBLE);
        if (winner == 1) {
            tvTttOverlayTitle.setText("⚔️\nYOU WIN!");
            tvTttOverlayTitle.setTextColor(ContextCompat.getColor(this, R.color.ttt_win_highlight));
            tvTttOverlaySubtitle.setText("The sword prevails!");
            playerWins++;
            tvPlayerScore.setText(String.valueOf(playerWins));

            // Save win to cloud
            UserDataManager udmWin = UserDataManager.getInstance(this);
            int totalWins = udmWin.getInt("wins_ttt", 0);
            udmWin.putInt("wins_ttt", totalWins + 1);

            // Award coins based on difficulty: Easy=5, Medium=15, Hard=25
            int coinReward = (difficulty == 0) ? 5 : (difficulty == 1) ? 15 : 25;
            int coins = udmWin.getInt("coins", 0);
            int totalEarned = udmWin.getInt("total_coins_earned", 0);
            java.util.Map<String, Object> winData = new java.util.HashMap<>();
            winData.put("coins", coins + coinReward);
            winData.put("total_coins_earned", totalEarned + coinReward);
            udmWin.putMultiple(winData);
            showCoinReward("⚔️ +" + coinReward + " 🪙");

            // Daily Challenge: Track consecutive wins
            int consecWins = udmWin.getInt("challenge_ttt_consec_wins", 0) + 1;
            udmWin.putInt("challenge_ttt_consec_wins", consecWins);
            if (consecWins >= 3) {
                udmWin.putBoolean("challenge_ttt_streak_done", true);
            }

            SoundManager.getInstance(this).playSfx("merge");
        } else if (winner == 2) {
            tvTttOverlayTitle.setText("🛡️\nCPU WINS!");
            tvTttOverlayTitle.setTextColor(ContextCompat.getColor(this, R.color.ttt_shield_color));
            tvTttOverlaySubtitle.setText("The shield holds!");
            cpuWins++;
            tvCpuScore.setText(String.valueOf(cpuWins));

            // Reset consecutive win streak on loss
            UserDataManager.getInstance(this).putInt("challenge_ttt_consec_wins", 0);

            SoundManager.getInstance(this).playSfx("game_over");
        } else {
            tvTttOverlayTitle.setText("🤝\nDRAW!");
            tvTttOverlayTitle.setTextColor(ContextCompat.getColor(this, R.color.ttt_subtitle_color));
            tvTttOverlaySubtitle.setText("Evenly matched!");

            // Reset consecutive win streak on draw
            UserDataManager.getInstance(this).putInt("challenge_ttt_consec_wins", 0);

            SoundManager.getInstance(this).playSfx("click");
        }

        tvTurnIndicator.setText("GAME OVER");
        tvTurnIndicator.setTextColor(ContextCompat.getColor(this, R.color.ttt_subtitle_color));
    }

    private void resetBoard() {
        board = new int[3][3];
        gameOver = false;
        playerTurn = true;
        tvTurnIndicator.setText("⚔️ YOUR TURN");
        tvTurnIndicator.setTextColor(ContextCompat.getColor(this, R.color.ttt_sword_color));

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                cellViews[r][c].setText("");
                cellViews[r][c].setTextColor(Color.TRANSPARENT);
                cellViews[r][c].setBackgroundResource(R.drawable.bg_ttt_cell);
            }
        }
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
            .setInterpolator(new android.view.animation.OvershootInterpolator(1.2f))
            .withEndAction(() -> coinRewardPopup.animate()
                .alpha(0f).translationY(-30f)
                .setStartDelay(1800)
                .setDuration(300)
                .withEndAction(() -> coinRewardPopup.setVisibility(View.GONE))
                .start())
            .start();
    }
}
