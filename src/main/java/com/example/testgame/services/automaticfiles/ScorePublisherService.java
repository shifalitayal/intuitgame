package com.example.testgame.services.automaticfiles;

import com.example.testgame.constants.Constant;
import com.example.testgame.models.Player;
import com.example.testgame.repositories.PlayerRepository;
import com.example.testgame.repositories.PlayerScoreRepository;
import com.example.testgame.services.IScorePublisher;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
//
//import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@Service
public class ScorePublisherService implements IScorePublisher {

    private static final Logger logger = Logger.getLogger(ScorePublisherService.class.getName());

//    private final String directoryPath = "src\\main\\resources\\files\\";
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
        this.fileCounter=2;
    }

    @PostConstruct
    public void init() {
        this.executorService = Executors.newFixedThreadPool(3); // Maximum of 3 threads
        startPublishingScores();
//        publishPlayerScores();
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

//    public void addEntryToFile(Integer playerId, String playerName, Integer playerScore) {
//        try (FileWriter writer = new FileWriter(currentFileName, true)) {
//            writer.write(playerId+ "," +playerName + "," + playerScore + ","+ LocalDateTime.now() +"\n");
//            currentFileSize++;
//
//            if (currentFileSize >= Constant.DEFAULT_FILE_SIZE) {
//                writer.close();
//                createNewFile();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    public String formatDate(){
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Extract the date part from LocalDateTime
        LocalDate date = now.toLocalDate();

        // Define a custom date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Format the LocalDate object as a string
        return date.format(formatter);

    }

    public void addEntryToFile(Integer playerId, String playerName, Integer playerScore) {
        synchronized (this) {
            try (FileWriter writer = new FileWriter(currentFileName, true)) {
                writer.write(playerId + "," + playerName + "," + playerScore + "," + LocalDateTime.now() + "\n");
                currentFileSize++;

                if (currentFileSize >= Constant.DEFAULT_FILE_SIZE) {
                    writer.close();
                    createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void createNewFile() {
        // Generate a new file name, you can use a random UUID or any other naming scheme
        String currentDate = formatDate();
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
                    List<Player> players = playerRepository.findAll();
                    for (Player player : players) {
                        Integer playerId = player.getPlayerId();
                        String playerName = player.getPlayerName();
                        int score = generateRandomScore();
                        addEntryToFile(playerId, playerName, score);
                    }
                    // Sleep for 15 seconds
                    Thread.sleep(15000);
                } catch (Exception e) {
                    logger.severe("Error in publishing scores: " + e.getMessage());
                }
            }
        }
    }
    /**
     *  it's a scheduled task that runs every 15 seconds
     * Generates random player scores and writes them to a file.
     * This method is invoked periodically by a scheduled task.
     */

//    @Scheduled(fixedRate = 15000) // Run every 1 second
//    public void publishPlayerScores() {
//        long startTime = System.currentTimeMillis();
//        Player player = generateRandomPlayerName();
//        assert player != null;
//        Integer playerId= player.getPlayerId();
//        String playerName = player.getPlayerName();
//        int score = generateRandomScore();
//        addEntryToFile(playerId,playerName,score);
//        try (FileWriter writer = new FileWriter(Constant.FILE_NAME, true)) {
//
//            writer.write(playerId+ "," +playerName + "," + score + ","+ LocalDateTime.now() +"\n");
//            logger.info("********Published to topic(file) successfully *********");
//            long endTime = System.currentTimeMillis();
//            long executionTime = endTime - startTime;
//            logger.info("Task execution time: " + executionTime + " milliseconds");
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
