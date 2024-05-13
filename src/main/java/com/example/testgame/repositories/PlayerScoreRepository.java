package com.example.testgame.repositories;

import com.example.testgame.models.PlayerScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlayerScoreRepository extends JpaRepository<PlayerScore,Integer> {

    List<PlayerScore> findTop5ByOrderByScoreDesc();
}
