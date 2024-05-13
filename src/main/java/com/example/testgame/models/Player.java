package com.example.testgame.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "player")
@Data
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @Column(name = "player_id")
    private Integer playerId;

    @Column
    private String playerName;


    public Player(String name)
    {
        this.playerName=name;
    }

    public Player(Integer playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

}
