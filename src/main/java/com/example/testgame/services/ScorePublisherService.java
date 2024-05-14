package com.example.testgame.services;

import com.example.testgame.constants.Constant;
import com.example.testgame.exceptions.NegativeScoreException;
import com.example.testgame.exceptions.PublishingFileScoreException;
import com.example.testgame.helper.DateFormatterHelper;
import com.example.testgame.models.Player;
import com.example.testgame.repositories.PlayerRepository;
import com.example.testgame.repositories.PlayerScoreRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
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

    String currentFileName;
    volatile int currentFileSize;

    static int fileCounter;

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

    /**
     * Adds a new entry to the file with the specified player ID, player name, and player score.
     * The entry format is "playerId,playerName,playerScore,timestamp\n".
     * If the player score is positive, the entry is appended to the current file.
     * If the player score is negative, a NegativeScoreException is thrown.
     * After adding the entry, the method checks if the current file size exceeds the default file size.
     * If so, it closes the current file and creates a new file.
     *
     * @param playerId The ID of the player.
     * @param playerName The name of the player.
     * @param playerScore The score of the player.
     * @throws PublishingFileScoreException If an error occurs while publishing the score to the file.
     * @throws NegativeScoreException If the player score is negative.
     */
    public void addEntryToFile(Integer playerId, String playerName, Integer playerScore) throws PublishingFileScoreException , NegativeScoreException {
        synchronized (this) {
            logger.info("Publishing score by thread " + Thread.currentThread() + "id " + Thread.currentThread().getId() + " name "+ Thread.currentThread().getName());
            try (FileWriter writer = new FileWriter(currentFileName, true)) {
                if(playerScore>0)
                {
                    writer.write(playerId + "," + playerName + "," + playerScore + "," + LocalDateTime.now() + "\n");
                    currentFileSize++;
                }
                else
                    throw new NegativeScoreException("Player Score Can not be negative ");


                if (currentFileSize >= Constant.DEFAULT_FILE_SIZE) {
                    writer.close();
                    createNewFile();
                }
            } catch (Exception e) {
                throw new PublishingFileScoreException("Error is thrown while publishing score to File",e);
            }
        }
    }

    /**
     * Creates a new file in the specified directory using a naming scheme based on the current date and a file counter.
     * The file name format is "file_[current_date]_[file_counter].txt".
     * The current date is obtained using the DateFormatterHelper.
     * The file counter is incremented after each file creation.
     * After creating the file, the current file name is updated, and the current file size is reset to zero.
     */

    public void createNewFile() {
        // Generate a new file name, you can use a random UUID or any other naming scheme
        DateFormatterHelper dateFormatterHelper = new DateFormatterHelper();
        String currentDate = dateFormatterHelper.formatDate();
        String newFileName = Constant.DIRECTORY_PATH + "file_" + currentDate + "_"+fileCounter+".txt";
        currentFileName = newFileName;
        fileCounter++;
        logger.info("current file name "+newFileName);
        currentFileSize = 0;
    }

    /**
     * A task responsible for publishing scores to a file at regular intervals.
     * This task generates random player names, scores, and player IDs, and adds them to the file.
     * It sleeps for 5 seconds between each publishing attempt.
     *
     * If an exception occurs during the publishing process, it logs the exception message using a logger.
     * If the exception is a PublishingFileScoreException or NegativeScoreException, it logs the error message as severe.
     * For any other exceptions, it logs a generic error message along with the exception message as severe.
     */
    public class ScorePublishTask implements Runnable {
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
                } catch (PublishingFileScoreException  | NegativeScoreException e) {
                    logger.severe(e.getMessage());
                } catch (Exception e)
                {
                    logger.severe("Error in generating player name or scores: " + e.getMessage());
                }
            }
        }
    }
}
