package com.example.pixelarcade;

public class LeaderboardEntry {
    private int rank;
    private String name;
    private String score;

    public LeaderboardEntry(int rank, String name, String score) {
        this.rank = rank;
        this.name = name;
        this.score = score;
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
}
