package com.example.testgame.controllers;

import com.example.testgame.models.PlayerScore;
import com.example.testgame.services.automaticfiles.ScoreConsumerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameController {

    @Autowired
    private ScoreConsumerService service;

    @GetMapping("/getTop5")
    public List<PlayerScore> getTop5()
    {
        return service.getTopScores();
    }
}
