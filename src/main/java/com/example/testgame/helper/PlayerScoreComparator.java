package com.example.testgame.helper;

import com.example.testgame.models.PlayerScore;

import java.util.Comparator;

public class PlayerScoreComparator implements Comparator<PlayerScore> {

    @Override
    public int compare(PlayerScore player1, PlayerScore player2) {
        // Compare scores in reverse order
        int scoreComparison = Integer.compare(player2.getScore(), player1.getScore());

        // If scores are equal, compare timestamps
        if (scoreComparison == 0) {
            return player1.getTimestamp().compareTo(player2.getTimestamp());
        }

        return scoreComparison;
    }
}