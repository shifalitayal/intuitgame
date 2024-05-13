package com.example.testgame.services;

import com.example.testgame.models.PlayerScore;

import java.io.IOException;
import java.util.List;

public interface IScoreConsumer {
    void processScoresFromFile() throws IOException;
    List<PlayerScore> getTopScores();
}
