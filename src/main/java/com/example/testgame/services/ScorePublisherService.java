package com.example.testgame.services;

import com.example.testgame.constants.Constant;
import com.example.testgame.exceptions.PublishingFileScoreException;
import com.example.testgame.helper.DateFormatterHelper;
import com.example.testgame.models.Player;
import com.example.testgame.repositories.PlayerRepository;
import com.example.testgame.repositories.PlayerScoreRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Service
public class ScorePublisherService implements IScorePublisher {

    private static final Logger logger = Logger.getLogger(ScorePublisherService.class.getName());
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerScoreRepository playerScoreRepository;

    private String currentFileName;
    private volatile int currentFileSize;

    private static int fileCounter;

    private ExecutorService executorService;


    public ScorePublisherService(){

        this.currentFileName = Constant.FILE_NAME;
        this.currentFileSize = 0;
        fileCounter=2;
    }

    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(3); // Maximum of 3 threads
        startPublishingScores();
    }

    public void startPublishingScores() {

        for (int i = 0; i < 3; i++) {
            executorService.execute(new ScorePublishTask());
        }
    }

    /**
     * Generates a random player from the list of players retrieved from the database.
     *
     * @return A randomly selected Player object from the list of players, or null if the list is empty.
     */

    public Player generateRandomPlayerName() {

        List<Player> playerList = playerRepository.findAll();
        Random random = new Random();

        if (!playerList.isEmpty()) {

            int randomIndex = random.nextInt(playerList.size());
            logger.info("*************Player Joined the Game.**********");
            return playerList.get(randomIndex);

        } else {
            return null;
        }
    }

    /**
     * Generates a random score between 0 and 100 (inclusive).
     *
     * @return A randomly generated integer score.
     */
    public int generateRandomScore() {
        logger.info("***** Player Completed his game **********");
        return (int) (Math.random() * 101);
    }

    public void addEntryToFile(Integer playerId, String playerName, Integer playerScore) throws PublishingFileScoreException {
        synchronized (this) {
            logger.info("Publishing score by thread " + Thread.currentThread() + "id " + Thread.currentThread().getId() + " name "+ Thread.currentThread().getName());
            try (FileWriter writer = new FileWriter(currentFileName, true)) {
                writer.write(playerId + "," + playerName + "," + playerScore + "," + LocalDateTime.now() + "\n");
                currentFileSize++;

                if (currentFileSize >= Constant.DEFAULT_FILE_SIZE) {
                    writer.close();
                    createNewFile();
                }
            } catch (IOException e) {
                throw new PublishingFileScoreException("Error is thrown while publishing score to File",e);
            }
        }
    }


    private void createNewFile() {
        // Generate a new file name, you can use a random UUID or any other naming scheme
        DateFormatterHelper dateFormatterHelper = new DateFormatterHelper();
        String currentDate = dateFormatterHelper.formatDate();
        String newFileName = Constant.DIRECTORY_PATH + "file_" + currentDate + "_"+fileCounter+".txt";
        currentFileName = newFileName;
        fileCounter++;
        logger.info("current file name "+newFileName);
        currentFileSize = 0;
    }

    private class ScorePublishTask implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Player player = generateRandomPlayerName();
                    Integer playerId = player.getPlayerId();
                    String playerName = player.getPlayerName();
                    int score = generateRandomScore();
                    addEntryToFile(playerId, playerName, score);
                    // Sleep for 5 seconds
                    Thread.sleep(5000);
                } catch (PublishingFileScoreException e) {
                    logger.severe(e.getMessage());
                }
                catch (Exception e)
                {
                    logger.severe("Error in generating player name or scores: " + e.getMessage());
                }
            }
        }
    }
}
