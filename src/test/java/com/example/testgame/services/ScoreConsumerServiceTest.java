package com.example.testgame.services;

import com.example.testgame.constants.Constant;
import com.example.testgame.exceptions.FileCountLineException;
import com.example.testgame.exceptions.ProcessingFileScoreException;
import com.example.testgame.exceptions.UpdatingCacheException;
import com.example.testgame.exceptions.UpdatingDBException;
import com.example.testgame.models.Player;
import com.example.testgame.models.PlayerScore;
import com.example.testgame.repositories.PlayerScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.PriorityBlockingQueue;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScoreConsumerServiceTest {

    @InjectMocks
    ScoreConsumerService scoreConsumerService;
    @Mock
    PlayerScoreRepository playerScoreRepository;

    @Mock
    private File mockDirectory;

    @Mock
    private File mockFile;

    @Mock
    private BufferedReader mockReader;

    @Mock
    private Map<String, Integer> mockLastProcessedLineMap;


    @TempDir
    static Path tempDir;

    @Mock
    private PriorityBlockingQueue<PlayerScore> cache;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
        scoreConsumerService.lastProcessedLineMap = mockLastProcessedLineMap;

    }

    @Test
    void testGetTopScores() {
        // Arrange
        List<PlayerScore> expectedTopScores = new ArrayList<>();
        expectedTopScores.add(new PlayerScore(1, new Player("Player1"), 200, LocalDateTime.now()));
        expectedTopScores.add(new PlayerScore(2, new Player("Player2"), 100, LocalDateTime.now()));
        // Add more player score objects as needed

        // Mock the cache to return the expected top scores
        when(cache.toArray()).thenReturn(expectedTopScores.toArray(new PlayerScore[0]));

        // Act
        List<PlayerScore> actualTopScores = scoreConsumerService.getTopScores();

        // Assert
        // Verify that the returned list contains the expected top 5 scores
        assertEquals(expectedTopScores, actualTopScores);
    }

    @Test
    void testRetrieveTopKScoresFromDatabase() {
        // Arrange
        List<PlayerScore> expectedScores = new ArrayList<>();
        // Add expected player score objects as needed

        // Mock the playerScoreRepository to return the expected scores
        when(playerScoreRepository.findTopkByOrderByScoreDesc(anyInt())).thenReturn(expectedScores);

        // Act
        List<PlayerScore> actualScores = scoreConsumerService.retrieveTopKScoresFromDatabase();

        // Assert
        // Verify that the method calls playerScoreRepository.findTopkByOrderByScoreDesc with the correct parameter
        verify(playerScoreRepository, times(1)).findTopkByOrderByScoreDesc(Constant.TOP_PLAYERS);
        // Verify that the returned list matches the expected scores
        assertEquals(expectedScores, actualScores);
    }

    @Test
    void testUpdateDatabase() throws UpdatingDBException {
        // Arrange
        int playerId = 1;
        String playerName = "Test Player";
        int score = 100;
        LocalDateTime timestamp = LocalDateTime.now();
        Player player = new Player(playerId, playerName);
        PlayerScore playerScore = new PlayerScore(player, score, timestamp);

        // Mock the playerScoreRepository
        when(playerScoreRepository.save(playerScore)).thenReturn(playerScore);

        scoreConsumerService.updateDatabase(playerId,playerName,score,timestamp);

        // Assert
        // Verify that the save method is called with the correct PlayerScore object
        verify(playerScoreRepository, times(1)).save(playerScore);
    }

    @Test
    void testUpdateDatabase_ExceptionThrown() {
        // Arrange
        int playerId = 1;
        String playerName = "Test Player";
        int score = 100;
        LocalDateTime timestamp = LocalDateTime.now();
        Player player = new Player(playerId, playerName);
        PlayerScore playerScore = new PlayerScore(player, score, timestamp);

        // Mock the playerScoreRepository to throw an exception
        doThrow(new RuntimeException("Database connection failed")).when(playerScoreRepository).save(playerScore);

        // Act & Assert
        assertThrows(UpdatingDBException.class, () -> scoreConsumerService.updateDatabase(playerId, playerName, score, timestamp));
    }

    @Test
    void testUpdateTopScoresInCache_EmptyCache() {
        // Arrange
        PlayerScore playerScore = new PlayerScore(new Player(1, "Test Player"), 100, LocalDateTime.now());
        List<PlayerScore> topScoresFromDB = new ArrayList<>();
        when(scoreConsumerService.retrieveTopKScoresFromDatabase()).thenReturn(topScoresFromDB);
        when(cache.isEmpty()).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> scoreConsumerService.updateTopScoresInCache(playerScore));
        // Verify that cache.addAll() is called with topScoresFromDB
        verify(cache, times(1)).addAll(topScoresFromDB);
    }

    @Test
    void testUpdateTopScoresInCache_CacheNotEmpty() {
        // Arrange
        PlayerScore playerScore = new PlayerScore(new Player(1, "Test Player"), 100, LocalDateTime.now());
        List<PlayerScore> topScoresFromDB = new ArrayList<>();
        // Mock cache to contain some initial data
        PlayerScore existingPlayerScore = new PlayerScore(new Player(2, "Existing Player"), 80, LocalDateTime.now());
        topScoresFromDB.add(existingPlayerScore);
        when(scoreConsumerService.retrieveTopKScoresFromDatabase()).thenReturn(topScoresFromDB);
        when(cache.isEmpty()).thenReturn(false);
        when(cache.size()).thenReturn(1); // Mock cache size to be less than TOP_PLAYERS

        // Act & Assert
        assertDoesNotThrow(() -> scoreConsumerService.updateTopScoresInCache(playerScore));
        // Verify that cache.add() is called with the new player score
        verify(cache, times(1)).add(playerScore);
    }

    @Test
    void testUpdateTopScoresInCache_CacheSizeGreaterThanTopPlayers() {
        // Arrange
        PlayerScore playerScore = new PlayerScore(new Player(1, "Test Player"), 100, LocalDateTime.now());
        List<PlayerScore> topScoresFromDB = new ArrayList<>();
        // Mock cache to contain more than Constant.TOP_PLAYERS scores
        topScoresFromDB.add(new PlayerScore(new Player(2, "Player 2"), 90, LocalDateTime.now()));
        topScoresFromDB.add(new PlayerScore(new Player(3, "Player 3"), 85, LocalDateTime.now()));
        topScoresFromDB.add(new PlayerScore(new Player(4, "Player 4"), 80, LocalDateTime.now()));
        topScoresFromDB.add(new PlayerScore(new Player(5, "Player 5"), 75, LocalDateTime.now()));
        topScoresFromDB.add(new PlayerScore(new Player(6, "Player 6"), 70, LocalDateTime.now()));
        when(scoreConsumerService.retrieveTopKScoresFromDatabase()).thenReturn(topScoresFromDB);
        when(cache.isEmpty()).thenReturn(false);
        when(cache.size()).thenReturn(6); // Mock cache size to be greater than Constant.TOP_PLAYERS
        when(cache.peek()).thenReturn(topScoresFromDB.get(4)); // Mock the lowest score in the cache

        // Act & Assert
        assertDoesNotThrow(() -> scoreConsumerService.updateTopScoresInCache(playerScore));
        // Verify that cache.poll() and cache.add() are called as expected
        verify(cache, times(1)).poll();
        verify(cache, times(1)).add(playerScore);
    }

    @Test
    void testUpdateTopScoresInCache_ExceptionThrown() {
        // Arrange
        PlayerScore playerScore = new PlayerScore(new Player(1, "Test Player"), 100, LocalDateTime.now());
        // Mocking an exception to be thrown when cache operations are performed
        doThrow(new NullPointerException("Mocked exception")).when(cache).isEmpty();

        // Act & Assert
        // Verify that UpdatingCacheException is thrown when cache operations fail
        assertThrows(NullPointerException.class, () -> scoreConsumerService.updateTopScoresInCache(playerScore));
    }

    @Test
    void testUpdateTopScoresInCache_UpdatingCacheExceptionThrown() {
        // Arrange
        PlayerScore playerScore = new PlayerScore(new Player(1, "Test Player"), 100, LocalDateTime.now());
        // Mocking cache operation to throw a ConcurrentModificationException
        doThrow(new ConcurrentModificationException("Mocked exception")).when(cache).add(playerScore);

        // Act & Assert
        // Verify that UpdatingCacheException is thrown when cache operations fail
        assertThrows(UpdatingCacheException.class, () -> scoreConsumerService.updateTopScoresInCache(playerScore));
    }

    @Test
    void testCountTotalLines() throws IOException, FileCountLineException {
        // Create a temporary file with some content
        Path tempFile = Files.createTempFile(tempDir, "test", ".txt");
        Files.write(tempFile, "Line 1\nLine 2\nLine 3".getBytes());

        // Call the method and assert the result
        int lineCount = scoreConsumerService.countTotalLines(tempFile.toFile());
        assertEquals(3, lineCount);
    }

    @Test
    void testCountTotalLinesWithEmptyFile() throws IOException, FileCountLineException {
        // Create a temporary empty file
        Path tempFile = Files.createTempFile(tempDir, "test", ".txt");

        // Call the method and assert the result
        int lineCount = scoreConsumerService.countTotalLines(tempFile.toFile());
        assertEquals(0, lineCount);
    }

    @Test
    void testCountTotalLinesWithNonExistingFile() {
        // Call the method with a non-existing file and assert that it throws the expected exception
        assertThrows(FileCountLineException.class, () -> scoreConsumerService.countTotalLines(new File("non_existing_file.txt")));
    }

    @Test
    void testProcessNewScoresFromFile() throws IOException, ProcessingFileScoreException {
        // Mock file, startLine, and endLine
        File mockFile = mock(File.class);
        int startLine = 1;
        int endLine = 5;

        // Stubbing method calls on the mockReader
        when(mockReader.readLine()).thenReturn(
                "1,John,100,2024-05-13T12:00:00",
                "2,Alice,200,2024-05-13T12:30:00",
                "3,Bob,150,2024-05-13T13:00:00",
                "4,Eve,180,2024-05-13T13:30:00"
        );

        // Call the method
        scoreConsumerService.processNewScoresFromFile(new File(Constant.FILE_NAME), startLine, endLine);

        // Verify that lastProcessedLineMap was updated
        verify(scoreConsumerService.lastProcessedLineMap).put(eq("file_2024-05-11_1.txt"), eq(endLine));
    }

    @Test
    void testProcessNewScoresFromFileWhenLineIsGreaterThanStartLine() throws IOException, ProcessingFileScoreException {
        // Mock file, startLine, and endLine
        File mockFile = mock(File.class);
        int startLine = 2;
        int endLine = 10;

        // Stubbing method calls on the mockReader
        when(mockReader.readLine()).thenReturn(
                "1,John,100,2024-05-13T12:00:00",
                "2,Alice,200,2024-05-13T12:30:00",
                "3,Bob,150,2024-05-13T13:00:00",
                "4,Eve,180,2024-05-13T13:30:00"
        );

        // Call the method
        scoreConsumerService.processNewScoresFromFile(new File(Constant.FILE_NAME), startLine, endLine);

        // Verify that lastProcessedLineMap was updated
        verify(scoreConsumerService.lastProcessedLineMap).put(eq("file_2024-05-11_1.txt"), eq(endLine));
    }
    @Test
    void testProcessNewScoresFromFileWithNullPointerException() throws IOException {
        // Mock file, startLine, and endLine

        int startLine = 1;
        int endLine = 5;

        // Stubbing method call on the mockReader to throw IOException
        when(mockReader.readLine()).thenThrow(new NullPointerException());

        // Call the method and expect an exception
        assertThrows(ProcessingFileScoreException.class, () -> {
            scoreConsumerService.processNewScoresFromFile(mockFile, startLine, endLine);
        });

    }
    @Test
    void testProcessScoresFromFileWithGenericException() throws ProcessingFileScoreException, FileCountLineException {
        // Stubbing method calls on mockDirectory
        when(mockDirectory.listFiles()).thenThrow(new RuntimeException("Mocked exception"));

        // Call the method
        scoreConsumerService.processScoresFromFile();

    }

    @Test
    void testProcessScoresFromFileWithExceptions() throws Exception {
        // Mock directory and files
        File directory = mock(File.class);
        when(directory.listFiles()).thenReturn(null);

        // Call the method
        scoreConsumerService.processScoresFromFile();

    }
}

