package com.example.testgame.services;

import com.example.testgame.models.Player;
import com.example.testgame.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceImpl {

    @Autowired
    PlayerRepository playerRepository;

    public Player addPlayer(Player player)
    {
        return playerRepository.save(player);
    }
}
