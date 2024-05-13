package com.example.testgame.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_score")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlayerScore implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Integer scoreId;

    @ManyToOne
    @JoinColumn(name = "player_id",referencedColumnName = "player_id")
    private Player player;

    @Column
    private Integer score;

    @Column
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;


    public PlayerScore(Player player, Integer score)
    {
        this.player=player;
        this.score=score;
        this.timestamp = LocalDateTime.now();
    }

    public PlayerScore(Player player, Integer score,LocalDateTime timestamp)
    {
        this.player=player;
        this.score=score;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "PlayerScore{" +
                "scoreId=" + scoreId +
                ", player=" + player +
                ", score=" + score +
                ", timestamp=" + timestamp +
                '}';
    }

}
