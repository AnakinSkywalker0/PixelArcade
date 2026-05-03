package com.example.pixelarcade.main;

public class LeaderboardEntry {
    private int rank;
    private String name;
    private String score;
    private boolean isCurrentUser;

    public LeaderboardEntry(int rank, String name, String score) {
        this(rank, name, score, false);
    }

    public LeaderboardEntry(int rank, String name, String score, boolean isCurrentUser) {
        this.rank = rank;
        this.name = name;
        this.score = score;
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

    public boolean isCurrentUser() {
        return isCurrentUser;
    }
}
