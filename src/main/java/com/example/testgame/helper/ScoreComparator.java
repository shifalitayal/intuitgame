package com.example.testgame.helper;

import com.example.testgame.models.PlayerScore;

import java.util.Comparator;

public class ScoreComparator implements Comparator<PlayerScore> {
    @Override
    public int compare(PlayerScore player1, PlayerScore player2) {

        return Integer.compare(player1.getScore(), player2.getScore());
    }

}
