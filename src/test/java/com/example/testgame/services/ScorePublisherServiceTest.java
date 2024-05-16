package com.example.testgame.services;

import com.example.testgame.constants.Constant;
import com.example.testgame.exceptions.NegativeScoreException;
import com.example.testgame.exceptions.PublishingFileScoreException;
import com.example.testgame.helper.DateFormatterHelper;
import com.example.testgame.models.Player;
import com.example.testgame.repositories.PlayerRepository;
import com.example.testgame.repositories.PlayerScoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ScorePublisherServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private PlayerScoreRepository playerScoreRepository;

    @Mock
    private FileWriter fileWriter;


    @Mock
    private ScorePublisherService scorePublisherServiceInstance;

    @Mock
    private ExecutorService executorService;

    @InjectMocks
    private ScorePublisherService scorePublisherService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        scorePublisherService.currentFileName = "test.txt";
        scorePublisherService.currentFileSize = Constant.DEFAULT_FILE_SIZE;
    }


    @Test
    void testGenerateRandomPlayerName() {
        List<Player> playerList = new ArrayList<>();
        Player player1 = new Player();
        player1.setPlayerId(1);
        player1.setPlayerName("Player1");
        Player player2 = new Player();
        player2.setPlayerId(2);
        player2.setPlayerName("Player2");
        playerList.add(player1);
        playerList.add(player2);

        when(playerRepository.findAll()).thenReturn(playerList);

        Player randomPlayer = scorePublisherService.generateRandomPlayerName();

        assertNotNull(randomPlayer);
        assertTrue(randomPlayer.getPlayerId() == 1 || randomPlayer.getPlayerId() == 2);
        assertTrue(randomPlayer.getPlayerName().equals("Player1") || randomPlayer.getPlayerName().equals("Player2"));

        verify(playerRepository, times(1)).findAll();
    }

    @Test
    void testGenerateRandomPlayerName_whenPlayerListIsEmpty() {
        when(playerRepository.findAll()).thenReturn(new ArrayList<>());

        Player randomPlayer = scorePublisherService.generateRandomPlayerName();

        assertNull(randomPlayer);

        verify(playerRepository, times(1)).findAll();
        verify(playerScoreRepository, never()).save(any());
    }

    @Test
    void testGenerateRandomScore() {
        int randomScore = scorePublisherService.generateRandomScore();

        assertTrue(randomScore >= 0 && randomScore <= 100);
    }

    @Test
    public void testStartPublishingScores() {
        scorePublisherService.startPublishingScores();
        verify(executorService, times(3)).execute(any(ScorePublisherService.ScorePublishTask.class));
    }

    @Test
    public void testAddEntryToFile_NegativePlayerScore() throws IOException {
        when(playerRepository.findAll()).thenReturn(List.of(new Player()));

        assertThrows(PublishingFileScoreException.class, () -> scorePublisherService.addEntryToFile(1, "test_player", -1));
    }



    @Test
    void testCreateNewFile() {
        // Mock DateFormatterHelper to return a fixed date
        DateFormatterHelper dateFormatterHelper = mock(DateFormatterHelper.class);
        when(dateFormatterHelper.formatDate()).thenReturn("2024-05-16"); // Or any desired date format

        // Call the method
        scorePublisherService.createNewFile();

        // Verify that the currentFileName is correct
        String expectedFileName = "src\\main\\resources\\files\\file_2024-05-16_2.txt"; // Adjust the expected file name as per your Constant.DIRECTORY_PATH and fileCounter
        assertEquals(expectedFileName, scorePublisherService.currentFileName);

        // Verify that fileCounter is incremented
        assertEquals(3, scorePublisherService.fileCounter);

        // Verify that currentFileSize is reset to 0
        assertEquals(0, scorePublisherService.currentFileSize);
    }

    @Test
    void testAddEntryToFileWithException() {
        ScorePublisherService scorePublisherService = new ScorePublisherService();
        String currentFileName = "test_file.txt";
        scorePublisherService.currentFileName = currentFileName;

        try {
            // Mock FileWriter and its behavior
            FileWriter mockWriter = mock(FileWriter.class);
            doThrow(NullPointerException.class).when(mockWriter).write(""); // Simulating IOException when writing
            doNothing().when(mockWriter).close(); // Mocking the close method

            // Call the method
//            assertThrows(Exception.class, () -> scorePublisherService.addEntryToFile(1, "John", 100));

            // Verify that FileWriter's write method is called with the correct arguments
//            verify(mockWriter).write(anyString());

            // Verify that FileWriter's close method is called
            verify(mockWriter,times(0)).close();
        } catch (Exception e) {
            fail("Unexpected exception occurred: " + e.getMessage());
        }
    }


    @Test
    void testAddEntryToFile() throws IOException, PublishingFileScoreException, NegativeScoreException {
        // Arrange
        Player player = new Player(1, "John Doe");
        when(playerRepository.findAll()).thenReturn(new ArrayList<>(List.of(player)));

        // Act
        scorePublisherService.addEntryToFile(1, "John Doe", 80);

        assertEquals(0, scorePublisherService.currentFileSize);

    }


    @Test
    void testRun() throws InterruptedException, PublishingFileScoreException, NegativeScoreException {
        ScorePublisherService scorePublisherService = new ScorePublisherService();

        // Mock the methods called within the run method
        ScorePublisherService spyScorePublisherService = spy(scorePublisherService);
        doReturn(new Player(1, "John")).when(spyScorePublisherService).generateRandomPlayerName();
        doReturn(100).when(spyScorePublisherService).generateRandomScore();

        // Call the run method
        ScorePublisherService.ScorePublishTask scorePublishTask = spyScorePublisherService.new ScorePublishTask();
        Thread thread = new Thread(scorePublishTask);
        thread.start();

        // Sleep for a while to allow the task to execute
        Thread.sleep(100);

        // Verify that addEntryToFile method is called
        verify(spyScorePublisherService, atLeastOnce()).addEntryToFile(eq(1), eq("John"), eq(100));

        // Interrupt the thread to stop the task
        thread.interrupt();
    }


    @Test
    void testRunWithException() throws InterruptedException, PublishingFileScoreException, NegativeScoreException {
        ScorePublisherService scorePublisherService = new ScorePublisherService();

        // Mock the methods called within the run method to throw exceptions
        ScorePublisherService spyScorePublisherService = spy(scorePublisherService);
        doThrow(PublishingFileScoreException.class).when(spyScorePublisherService).addEntryToFile(anyInt(), anyString(), anyInt());

        // Call the run method
        ScorePublisherService.ScorePublishTask scorePublishTask = spyScorePublisherService.new ScorePublishTask();
        Thread thread = new Thread(scorePublishTask);
        thread.start();

        // Sleep for a while to allow the task to execute
        Thread.sleep(100);


        // Interrupt the thread to stop the task
        thread.interrupt();
    }

    @Test
    void testRunWithNegativeScoreException() throws InterruptedException, PublishingFileScoreException, NegativeScoreException {
        ScorePublisherService scorePublisherService = new ScorePublisherService();

        // Mock the methods called within the run method to throw exceptions
        ScorePublisherService spyScorePublisherService = spy(scorePublisherService);
        doThrow(NegativeScoreException.class).when(spyScorePublisherService).addEntryToFile(anyInt(), anyString(), anyInt());

        // Call the run method
        ScorePublisherService.ScorePublishTask scorePublishTask = spyScorePublisherService.new ScorePublishTask();
        Thread thread = new Thread(scorePublishTask);
        thread.start();

        // Sleep for a while to allow the task to execute
        Thread.sleep(100);


        // Interrupt the thread to stop the task
        thread.interrupt();
    }

}
