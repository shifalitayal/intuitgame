package com.example.testgame.services;

import com.example.testgame.exceptions.NegativePlayerIdOrPlayerNullException;
import com.example.testgame.models.Player;
import com.example.testgame.repositories.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PlayerServiceImpl {

    @Autowired
    PlayerRepository playerRepository;

    public Player addPlayer(Player player) throws NegativePlayerIdOrPlayerNullException {
        if(player!=null && player.getPlayerId()>0)
        return playerRepository.save(player);
        else
            throw new NegativePlayerIdOrPlayerNullException("Player Id can not be negative or Player can not be null");
    }
}
