//package com.example.testgame.services;
//
//import com.example.testgame.constants.Constant;
//import com.example.testgame.models.Player;
//import com.example.testgame.models.PlayerScore;
//import com.example.testgame.repositories.PlayerScoreRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.time.Month;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.logging.Logger;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class ScoreConsumerServiceTest {
//
//    @Mock
//    private PlayerScoreRepository playerScoreRepository;
//
//    @InjectMocks
//    private ScoreConsumerService scoreConsumerService;
//
//
//    @Mock
//    private BufferedReader mockBufferedReader;
//
//    private List<PlayerScore> samplePlayerScores;
//
//
//    @Mock
//    private Logger mockLogger;
//    @Mock
//    private Path mockedPath;
//
//    @BeforeEach
//    public void setUp() throws IOException {
//        samplePlayerScores = new ArrayList<>();
//        Player player1 = new Player(1, "Alice");
//        Player player2 = new Player(2, "Bob");
//        PlayerScore score1 = new PlayerScore(player1, 100, LocalDateTime.now());
//        PlayerScore score2 = new PlayerScore(player2, 200, LocalDateTime.now());
//        samplePlayerScores.add(score1);
//        samplePlayerScores.add(score2);
//    }
//
//    @Test
//    public void testProcessScoresFromFile() {
//        // Mocking the playerScoreRepository behavior
//        when(playerScoreRepository.findTop5ByOrderByScoreDesc()).thenReturn(samplePlayerScores);
//        when(Paths.get(Constant.FILE_NAME)).thenReturn(Path.of("file.txt"));
//        // Calling the method to be tested
//        scoreConsumerService.processScoresFromFile();
//
//        // Verifying that the method updates the cache correctly
//        List<PlayerScore> topScores = scoreConsumerService.getTopScores();
//        assertEquals(2, topScores.size());
//        assertEquals(200, topScores.get(0).getScore());
//        assertEquals("Bob", topScores.get(0).getPlayer().getPlayerName());
//        assertEquals(100, topScores.get(1).getScore());
//        assertEquals("Alice", topScores.get(1).getPlayer().getPlayerName());
//
//        // Verifying that the repository method is called
//        verify(playerScoreRepository, times(2)).findTop5ByOrderByScoreDesc();
//    }
//
//    @Test
//    public void testUpdateDatabaseAndCacheWithMoreThan5Scores() {
//        // Setting up initial cache
//        scoreConsumerService.cache = new ArrayList<>();
//        Player player = new Player(1, "Alice");
//
//        PlayerScore playerScore = new PlayerScore(player, 150, LocalDateTime.now());
//        scoreConsumerService.cache.add(new PlayerScore(player, 100, LocalDateTime.now()));
//        scoreConsumerService.cache.add(new PlayerScore(player, 120, LocalDateTime.now()));
//        scoreConsumerService.cache.add(new PlayerScore(player, 130, LocalDateTime.now()));
//        scoreConsumerService.cache.add(new PlayerScore(player, 140, LocalDateTime.now()));
//        scoreConsumerService.cache.add(new PlayerScore(player, 160, LocalDateTime.now()));
//
//        // Calling the method to be tested
//        scoreConsumerService.updateDatabaseAndCache(player.getPlayerId(),player.getPlayerName(),150,LocalDateTime.now());
//
//
//        // Verifying that the cache is updated correctly
//        List<PlayerScore> topScores = scoreConsumerService.getTopScores();
//        assertEquals(5, topScores.size());
//        assertEquals(120, topScores.get(4).getScore()); // The last score should be the new score
//    }
//
//    @Test
//    public void testUpdateDatabaseAndCacheWithCacheEmpty(){
//        // Setting up initial cache
//        List<PlayerScore> list = new ArrayList<>();
//        scoreConsumerService.cache = new ArrayList<>();
//        Player player = new Player(1, "Alice");
//        int score = 150;
//        list.add(new PlayerScore(player,150,LocalDateTime.now()));
//        when(scoreConsumerService.retrieveTop5ScoresFromDatabase()).thenReturn(list);
//
//        // Calling the method to be tested
//        scoreConsumerService.updateDatabaseAndCache(player.getPlayerId(),player.getPlayerName(),score,LocalDateTime.now());
//
//        // Verifying that the cache is updated correctly
//        List<PlayerScore> topScores = scoreConsumerService.getTopScores();
//        assertEquals(1, topScores.size());
//        assertEquals(150, topScores.get(0).getScore()); // The last score should be the new score
//    }
//
//    @Test
//    public void testUpdateTopScoresInCacheWithLessThan5Elements() {
//        // Mocking player score objects
//        List<PlayerScore> list = new ArrayList<>();
//
//        Player player = new Player(1, "Alice");
//        PlayerScore playerScore = new PlayerScore(player, 150, LocalDateTime.now());
//        list.add(playerScore);
//        list.add(new PlayerScore(player, 100, LocalDateTime.now()));
//        list.add(new PlayerScore(player, 120, LocalDateTime.now()));
//        when(scoreConsumerService.retrieveTop5ScoresFromDatabase()).thenReturn(list);
//        // Setting up cache with less than 5 elements
//        scoreConsumerService.cache = new ArrayList<>();
//        scoreConsumerService.cache.add(new PlayerScore(player, 100, LocalDateTime.now()));
//        scoreConsumerService.cache.add(new PlayerScore(player, 120, LocalDateTime.now()));
//
//        // Calling the method to be tested
//        scoreConsumerService.updateDatabaseAndCache(player.getPlayerId(),player.getPlayerName(),150,LocalDateTime.now());
//        // Verifying that the cache is updated correctly
//        List<PlayerScore> topScores = scoreConsumerService.getTopScores();
//        assertEquals(3, topScores.size()); // Only one new score should be added
//        assertEquals(100, topScores.get(2).getScore()); // The last score should be the new score
//    }
//
//    @Test
//    public void testUpdateCacheWithSameScore() {
//        // Setting up initial cache
//        scoreConsumerService.cache = new ArrayList<>();
//        Player player = new Player(1, "Alice");
//        Player player1 =new Player(2,"Bob");
//
//        PlayerScore playerScore = new PlayerScore(player, 150, LocalDateTime.now());
//        scoreConsumerService.cache.add(new PlayerScore(player, 100, LocalDateTime.now()));
//        scoreConsumerService.cache.add(new PlayerScore(player1, 100, LocalDateTime.of(2024, Month.APRIL, 1, 10, 30, 0)));
//        scoreConsumerService.cache.add(new PlayerScore(player, 130, LocalDateTime.now()));
//        scoreConsumerService.cache.add(new PlayerScore(player, 140, LocalDateTime.now()));
//        scoreConsumerService.cache.add(new PlayerScore(player, 160, LocalDateTime.now()));
//
//        // Calling the method to be tested
//        scoreConsumerService.updateTopScoresInCache(playerScore);
//
//        // Verifying that the cache is updated correctly
//        List<PlayerScore> topScores = scoreConsumerService.getTopScores();
//        assertEquals(5, topScores.size());
//        assertEquals(150, topScores.get(1).getScore()); // The last score should be the new score
//        assertEquals(player1,topScores.get(4).getPlayer());
//    }
////
////    @Test
////    public void testProcessScoresFromFile_IOException() throws IOException {
////        // Mocking behavior for countTotalLines() method to throw IOException
////        Path mockedPath = Paths.get(Constant.FILE_NAME);
////        when(scoreConsumerService.countTotalLines(mockedPath)).thenThrow(IOException.class);
////
////        // Verifying that IOException is propagated
////        assertThrows(IOException.class, () -> scoreConsumerService.processScoresFromFile());
////
////        // Verifying that logger.severe() is called with the appropriate message
////        verify(scoreConsumerService.logger).severe("Error occurred while processing scores from file: null");
////    }
////
////    @Test
////    public void testProcessNewScoresFromFile() throws IOException {
////        // Mock behavior of FileReader and BufferedReader
////        BufferedReader mockBufferedReader = Mockito.mock(BufferedReader.class);
////
////// Stubbing a method call on the mock object
//////        when(mockBufferedReader.readLine()).thenReturn("Mocked line");
////
////// Now you can use the mock object in your test
////        String line = mockBufferedReader.readLine();
////        when(new FileReader(Constant.FILE_NAME)).thenReturn(mock(FileReader.class));
////        when(mockBufferedReader.readLine()).thenReturn("1,John,100,2024-05-10T12:00:00");
////
////        // Call the method under test
////        scoreConsumerService.processNewScoresFromFile(1, 2);
////
////        // Verify that the expected methods were called
////        verify(mockLogger).info("********* Consuming from file **********");
////        verify(mockLogger).info("********** Updating Database ******");
//////        verify(scoreConsumerService).updateDatabaseAndCache(1, "John", 100, LocalDateTime.parse("2024-05-10T12:00:00"));
////    }
//
//}
