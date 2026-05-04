package com.example.pixelarcade.main;

public class LeaderboardEntry {
    private int rank;
    private String name;
    private String score;
    private String scoreLabel;
    private boolean isCurrentUser;

    public LeaderboardEntry(int rank, String name, String score, String scoreLabel) {
        this(rank, name, score, scoreLabel, false);
    }

    public LeaderboardEntry(int rank, String name, String score, String scoreLabel, boolean isCurrentUser) {
        this.rank = rank;
        this.name = name;
        this.score = score;
        this.scoreLabel = scoreLabel;
        this.isCurrentUser = isCurrentUser;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public String getScore() {
        return score;
    }

    public String getScoreLabel() {
        return scoreLabel;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }
}
