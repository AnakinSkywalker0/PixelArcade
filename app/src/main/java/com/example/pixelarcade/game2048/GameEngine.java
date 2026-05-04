package com.example.pixelarcade;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameEngine {

    private int[][] board;
    private int[][] previousBoard;
    private int size;
    private int score = 0;
    private int previousScore = 0;
    private boolean canUndo = false;
    private Random random = new Random();

    public GameEngine(int size) {
        this.size = size;
        this.board = new int[size][size];
        initBoard();
    }

    private void initBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = 0;
            }
        }
        score = 0;
        spawnTileWithRecord(null);
        spawnTileWithRecord(null);
    }

    public int[][] getBoard() {
        return board;
    }

    public int getScore() {
        return score;
    }

    public int getSize() {
        return size;
    }

    public static class MoveEvent {
        public int fromRow, fromCol;
        public int toRow, toCol;
        public int value;
        public boolean isMerge;
        public boolean isNew;

        public MoveEvent(int fr, int fc, int tr, int tc, int v, boolean m, boolean n) {
            fromRow = fr; fromCol = fc; toRow = tr; toCol = tc;
            value = v; isMerge = m; isNew = n;
        }
    }

    private void saveState() {
        previousBoard = new int[size][size];
        for (int i = 0; i < size; i++) {
            previousBoard[i] = board[i].clone();
        }
        previousScore = score;
        canUndo = true;
    }

    public boolean canUndo() {
        return canUndo;
    }

    public void undo() {
        if (canUndo && previousBoard != null) {
            board = previousBoard;
            score = previousScore;
            canUndo = false;
        }
    }

    public List<MoveEvent> moveLeft() {
        List<MoveEvent> currentMoves = new ArrayList<>();
        saveState();
        boolean moved = false;
        for (int i = 0; i < size; i++) {
            List<Integer> tiles = new ArrayList<>();
            List<Integer> originalCols = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                if (board[i][j] != 0) {
                    tiles.add(board[i][j]);
                    originalCols.add(j);
                }
            }

            int[] newLine = new int[size];
            boolean[] merged = new boolean[tiles.size()];
            int targetCol = 0;
            for (int k = 0; k < tiles.size(); k++) {
                if (k < tiles.size() - 1 && !merged[k] && tiles.get(k).equals(tiles.get(k + 1))) {
                    int newValue = tiles.get(k) * 2;
                    newLine[targetCol] = newValue;
                    score += newValue;
                    merged[k] = true;
                    merged[k + 1] = true;
                    currentMoves.add(new MoveEvent(i, originalCols.get(k), i, targetCol, tiles.get(k), true, false));
                    currentMoves.add(new MoveEvent(i, originalCols.get(k + 1), i, targetCol, tiles.get(k + 1), true, false));
                    moved = true;
                    targetCol++;
                } else if (!merged[k]) {
                    newLine[targetCol] = tiles.get(k);
                    if (originalCols.get(k) != targetCol) moved = true;
                    currentMoves.add(new MoveEvent(i, originalCols.get(k), i, targetCol, tiles.get(k), false, false));
                    targetCol++;
                }
            }
            board[i] = newLine;
        }
        if (moved) spawnTileWithRecord(currentMoves);
        return currentMoves;
    }

    public List<MoveEvent> moveRight() {
        List<MoveEvent> currentMoves = new ArrayList<>();
        saveState();
        boolean moved = false;
        for (int i = 0; i < size; i++) {
            List<Integer> tiles = new ArrayList<>();
            List<Integer> originalCols = new ArrayList<>();
            for (int j = size - 1; j >= 0; j--) {
                if (board[i][j] != 0) {
                    tiles.add(board[i][j]);
                    originalCols.add(j);
                }
            }

            int[] newLine = new int[size];
            int target = size - 1;
            boolean[] merged = new boolean[tiles.size()];
            for (int k = 0; k < tiles.size(); k++) {
                if (k < tiles.size() - 1 && !merged[k] && tiles.get(k).equals(tiles.get(k + 1))) {
                    int newValue = tiles.get(k) * 2;
                    newLine[target] = newValue;
                    score += newValue;
                    merged[k] = true; merged[k+1] = true;
                    currentMoves.add(new MoveEvent(i, originalCols.get(k), i, target, tiles.get(k), true, false));
                    currentMoves.add(new MoveEvent(i, originalCols.get(k+1), i, target, tiles.get(k+1), true, false));
                    moved = true; target--;
                } else if (!merged[k]) {
                    newLine[target] = tiles.get(k);
                    if (originalCols.get(k) != target) moved = true;
                    currentMoves.add(new MoveEvent(i, originalCols.get(k), i, target, tiles.get(k), false, false));
                    target--;
                }
            }
            board[i] = newLine;
        }
        if (moved) spawnTileWithRecord(currentMoves);
        return currentMoves;
    }

    public List<MoveEvent> moveUp() {
        List<MoveEvent> currentMoves = new ArrayList<>();
        saveState();
        boolean moved = false;
        for (int j = 0; j < size; j++) {
            List<Integer> tiles = new ArrayList<>();
            List<Integer> originalRows = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                if (board[i][j] != 0) {
                    tiles.add(board[i][j]);
                    originalRows.add(i);
                }
            }

            int targetRow = 0;
            boolean[] merged = new boolean[tiles.size()];
            int[] newCol = new int[size];
            for (int k = 0; k < tiles.size(); k++) {
                if (k < tiles.size() - 1 && !merged[k] && tiles.get(k).equals(tiles.get(k + 1))) {
                    int newValue = tiles.get(k) * 2;
                    newCol[targetRow] = newValue;
                    score += newValue;
                    merged[k] = true; merged[k+1] = true;
                    currentMoves.add(new MoveEvent(originalRows.get(k), j, targetRow, j, tiles.get(k), true, false));
                    currentMoves.add(new MoveEvent(originalRows.get(k+1), j, targetRow, j, tiles.get(k+1), true, false));
                    moved = true; targetRow++;
                } else if (!merged[k]) {
                    newCol[targetRow] = tiles.get(k);
                    if (originalRows.get(k) != targetRow) moved = true;
                    currentMoves.add(new MoveEvent(originalRows.get(k), j, targetRow, j, tiles.get(k), false, false));
                    targetRow++;
                }
            }
            for (int i = 0; i < size; i++) board[i][j] = newCol[i];
        }
        if (moved) spawnTileWithRecord(currentMoves);
        return currentMoves;
    }

    public List<MoveEvent> moveDown() {
        List<MoveEvent> currentMoves = new ArrayList<>();
        saveState();
        boolean moved = false;
        for (int j = 0; j < size; j++) {
            List<Integer> tiles = new ArrayList<>();
            List<Integer> originalRows = new ArrayList<>();
            for (int i = size - 1; i >= 0; i--) {
                if (board[i][j] != 0) {
                    tiles.add(board[i][j]);
                    originalRows.add(i);
                }
            }

            int targetRow = size - 1;
            boolean[] merged = new boolean[tiles.size()];
            int[] newCol = new int[size];
            for (int k = 0; k < tiles.size(); k++) {
                if (k < tiles.size() - 1 && !merged[k] && tiles.get(k).equals(tiles.get(k + 1))) {
                    int newValue = tiles.get(k) * 2;
                    newCol[targetRow] = newValue;
                    score += newValue;
                    merged[k] = true; merged[k+1] = true;
                    currentMoves.add(new MoveEvent(originalRows.get(k), j, targetRow, j, tiles.get(k), true, false));
                    currentMoves.add(new MoveEvent(originalRows.get(k+1), j, targetRow, j, tiles.get(k+1), true, false));
                    moved = true; targetRow--;
                } else if (!merged[k]) {
                    newCol[targetRow] = tiles.get(k);
                    if (originalRows.get(k) != targetRow) moved = true;
                    currentMoves.add(new MoveEvent(originalRows.get(k), j, targetRow, j, tiles.get(k), false, false));
                    targetRow--;
                }
            }
            for (int i = 0; i < size; i++) board[i][j] = newCol[i];
        }
        if (moved) spawnTileWithRecord(currentMoves);
        return currentMoves;
    }

    private void spawnTileWithRecord(List<MoveEvent> currentMoves) {
        int tilesToSpawn = 1;
        if (score >= 8000) {
            tilesToSpawn = 3;
        } else if (score >= 2000) {
            tilesToSpawn = 2;
        }

        for (int t = 0; t < tilesToSpawn; t++) {
            List<int[]> emptyCells = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (board[i][j] == 0) emptyCells.add(new int[]{i, j});
                }
            }
            if (!emptyCells.isEmpty()) {
                int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
                
                int val = 2;
                int rand = random.nextInt(100);
                
                if (score >= 10000) {
                    // 10% chance for 8, 40% chance for 4, 50% chance for 2
                    if (rand < 10) val = 8;
                    else if (rand < 50) val = 4;
                } else if (score >= 5000) {
                    // 30% chance for 4
                    if (rand < 30) val = 4;
                } else if (score >= 1000) {
                    // 20% chance for 4
                    if (rand < 20) val = 4;
                } else {
                    // 10% chance for 4 (Default)
                    if (rand < 10) val = 4;
                }
                
                board[cell[0]][cell[1]] = val;
                if (currentMoves != null) {
                    currentMoves.add(new MoveEvent(-1, -1, cell[0], cell[1], val, false, true));
                }
            } else {
                break;
            }
        }
    }

    public boolean isGameOver() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 0) return false; // Empty cell exists
                // Check right neighbor
                if (j < size - 1 && board[i][j] == board[i][j + 1]) return false;
                // Check bottom neighbor
                if (i < size - 1 && board[i][j] == board[i + 1][j]) return false;
            }
        }
        return true; // No empty cells, no adjacent matches
    }

    public int getMaxTile() {
        int max = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] > max) max = board[i][j];
            }
        }
        return max;
    }

    public boolean hasWon() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] == 2048) return true;
            }
        }
        return false;
    }

    public boolean hasTile(int value) {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (board[i][j] >= value) return true;
            }
        }
        return false;
    }

    public void resetBoard() {
        initBoard();
    }
}
