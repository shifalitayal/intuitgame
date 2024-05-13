package com.example.testgame.repositories;

import com.example.testgame.models.PlayerScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerScoreRepository extends JpaRepository<PlayerScore,Integer> {

    @Query("SELECT ps FROM PlayerScore ps ORDER BY ps.score DESC LIMIT ?1")
    List<PlayerScore> findTopkByOrderByScoreDesc(int k);

    @Query("SELECT ps FROM PlayerScore ps WHERE (ps.player.playerId, ps.score) IN (" +
            "    SELECT ps2.player.playerId, MAX(ps2.score) " +
            "    FROM PlayerScore ps2 " +
            "    GROUP BY ps2.player.playerId" +
            ") ORDER BY ps.score DESC LIMIT ?1")
    List<PlayerScore> findTopkScoreByPlayerIdOrderByMaxScoreDesc(int k);
}
