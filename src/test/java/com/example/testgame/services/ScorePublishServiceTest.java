//package com.example.testgame.services;
//
//import com.example.testgame.models.Player;
//import com.example.testgame.models.PlayerScore;
//import com.example.testgame.repositories.PlayerRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Logger;
//
//import static org.junit.jupiter.api.Assertions.assertNull;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//class ScorePublishServiceTest {
//
//
//    @InjectMocks
//    private ScorePublishService scorePublishService;
//
//    @Mock
//    private FileWriter fileWriter;
//
//
//    @Mock
//    private Logger logger;
//
//    @Mock
//    private PlayerRepository playerRepository;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//    }
//
//    @Test
//    public void testInit() {
//        // Mock any necessary behavior of playerRepository or playerScoreRepository
//
//        // Call the init() method
//        scorePublishService.init();
//
////        doNothing().when(scorePublishService.publishPlayerScores())
//        // Verify that publishPlayerScores() method is called
//        verify(scorePublishService).publishPlayerScores();
//    }
//
//    @Test
//    public void testGenerateRandomPlayerName() {
//        // Arrange
//        List<Player> playerList = new ArrayList<>();
//        playerList.add(new Player(1, "John"));
//        when(playerRepository.findAll()).thenReturn(playerList);
//
//        // Act
//        Player player = scorePublishService.generateRandomPlayerName();
//
//        // Assert
//        assert player != null;
//        assert player.getPlayerId() == 1;
//        assert "John".equals(player.getPlayerName());
//    }
//
//    @Test
//    public void testGenerateRandomPlayerNameIfNull() {
//        // Arrange
//        List<Player> playerList = new ArrayList<>();
//        when(playerRepository.findAll()).thenReturn(playerList);
//
//        // Act
//        Player player = scorePublishService.generateRandomPlayerName();
//
//        // Assert
//        assertNull(player);
//    }
//
//
//    @Test
//    public void testGenerateRandomScore() {
//        // Act
//        int score = scorePublishService.generateRandomScore();
//
//        // Assert
//        assert score >= 0 && score <= 100;
//
//    }
//
//    @Test
//    public void testPublishPlayersAndScores() throws IOException {
//        Player player = new Player(1,"John");
//        List<Player> playerList = new ArrayList<>();
//        playerList.add(player);
//        when(playerRepository.findAll()).thenReturn(playerList);
//        when(scorePublishService.generateRandomPlayerName()).thenReturn(player);
//        when(scorePublishService.generateRandomScore()).thenReturn(30);
//        doNothing().when(fileWriter).write(anyString());
//
//        scorePublishService.publishPlayerScores();
//
//        verify(fileWriter).write(anyString());
////        verify(logger).info("********Published to topic(file) successfully *********");
//
//
//    }
//
//    @Test
//    public void testPublishPlayerScores() throws IOException {
//        // Mock generateRandomPlayerName() method
//        Player player = new Player(1, "Test Player");
//        List<Player> playerList = new ArrayList<>();
//        playerList.add(new Player(1, "John"));
//        when(playerRepository.findAll()).thenReturn(playerList);
//
//        when(scorePublishService.generateRandomPlayerName()).thenReturn(playerList.get(0));
//
//        // Mock generateRandomScore() method
//        when(scorePublishService.generateRandomScore()).thenReturn(100);
//
//        // Call the publishPlayerScores() method
//        scorePublishService.publishPlayerScores();
//
//
//        // Verify FileWriter is called with correct arguments
////        verify(fileWriter).write("1,Test Player,100,2024-05-05T12:00:00\n");
//
//        // Verify logger is called with correct message
//        verify(logger).info("********Published to topic(file) successfully *********");
//    }
//
//
//}