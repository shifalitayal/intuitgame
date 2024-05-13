package com.example.testgame.services;

import com.example.testgame.constants.Constant;
import com.example.testgame.exceptions.FileCountLineException;
import com.example.testgame.exceptions.ProcessingFileScoreException;
import com.example.testgame.exceptions.UpdatingCacheException;
import com.example.testgame.exceptions.UpdatingDBException;
import com.example.testgame.helper.PlayerScoreComparator;
import com.example.testgame.helper.ScoreComparator;
import com.example.testgame.models.Player;
import com.example.testgame.models.PlayerScore;
import com.example.testgame.repositories.PlayerScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Logger;

@Component
public class ScoreConsumerService implements IScoreConsumer {

    static final Logger logger = Logger.getLogger(ScoreConsumerService.class.getName());

    PriorityBlockingQueue<PlayerScore> cache ;

    ExecutorService executorService ;


    private final Map<String, Integer> lastProcessedLineMap ;

    @Autowired
    private PlayerScoreRepository playerScoreRepository;

    public ScoreConsumerService()
    {
        this.cache = new PriorityBlockingQueue<>(Constant.TOP_PLAYERS,new ScoreComparator());
        this.executorService = Executors.newFixedThreadPool(2);
        this.lastProcessedLineMap  = new HashMap<>();
    }

    /**
     * Run after every 10s to process/consume scores from file
     * To add only the newly added entries from the file to the database, It keeps track of the last processed line
     * number in the file.
     * During each execution of the scheduled task, we count the total number of lines in the file.
     * If the lastProcessedLineNumber is less than the total number of lines in the file, it indicates that there
     * are new entries to be processed.
     * We then process only the new entries from the file starting from the line after the lastProcessedLineNumber
     * up to the last line in the file.
     */
    @Scheduled(fixedRate = 10000)
    public void processScoresFromFile() {
        try {

            File directory = new File(Constant.DIRECTORY_PATH);
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        int totalLines = countTotalLines(file);
                        int lastProcessedLine = lastProcessedLineMap.getOrDefault(file.getName(), 0);
                        if (lastProcessedLine < totalLines) {
                            logger.info("***** Processing new entries ******");
                            processNewScoresFromFile(file, lastProcessedLine + 1, totalLines);
                        }
                    }
                }
            }

        } catch (ProcessingFileScoreException | FileCountLineException e) {
            logger.severe(e.getMessage());
        } catch (Exception e) {
            logger.severe("Exception occurred in processScoresFromFile method "+e.getMessage());
        }
    }

    /**
     * Processes player scores from the file within the specified line range.
     *
     * @param startLine The line number from which to start processing (inclusive).
     * @param endLine   The line number at which to end processing (exclusive).
     *                  If endLine is greater than the total number of lines in the file,
     *                  processing will continue until the end of the file is reached.
     */

    public void processNewScoresFromFile(File file,int startLine, int endLine) throws ProcessingFileScoreException {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            logger.info("********* Consuming from file **********");
            while ((line = reader.readLine()) != null && lineNumber < endLine) {
                logger.info("line "+line);
                lineNumber++;
                if (lineNumber >= startLine) {
                    String[] parts = line.split(",");

                    int playerId = Integer.parseInt(parts[0]);
                    String playerName = parts[1];
                    int score = Integer.parseInt(parts[2]);
                    LocalDateTime timestamp = LocalDateTime.parse(parts[3]);

                    logger.info("********** Updating Database And Cache *********");

                    executeMultipleTask(playerId, playerName,score, timestamp);
                }
            }
            lastProcessedLineMap.put(file.getName(), endLine);
        } catch (IOException e) {
            throw new ProcessingFileScoreException("Error occurred while Processing File score",e);
        }

    }

    /**
     * Updates the top scores cache with a new player score if it is higher than the current lowest score in the cache.
     * overall time complexity of the updateTopScoresInCache method is O(log n), where n is the size of the priority queue (cache)
     * @param playerScore The player score to be considered for updating the cache.
     */

    public synchronized void updateTopScoresInCache(PlayerScore playerScore) throws UpdatingCacheException {

        logger.info("Cache is being updated by thread " + Thread.currentThread().getId() + " name " + Thread.currentThread().getName());
        try {
            if (cache.isEmpty()) {
                List<PlayerScore> getTop5FromDB = retrieveTopKScoresFromDatabase();
                logger.info("updated from db");
                cache.addAll(getTop5FromDB);
            } else {
                if (cache.size() < Constant.TOP_PLAYERS) //when db will be populated with toally new scores otherwise always one entry in cache
                {
                    cache.add(playerScore);
                } else {
                    logger.info("cache size in else by " + cache.size());
                    PlayerScore lowestScorePlayer = cache.peek();
                    assert lowestScorePlayer != null;
                    if (playerScore.getScore() > lowestScorePlayer.getScore()) {
                        cache.poll();
                        cache.add(playerScore);
                    }
                }

            }
        } catch (DataAccessException e) {
            throw new UpdatingCacheException("Error occurred while updating cache",e);
        }catch (NullPointerException e){
            throw new NullPointerException("Null pointer exception occurred while updating cache");
        } catch (Exception e) {
            logger.severe(e.getMessage());
        }
    }





    /**
     * Updates the database with a new player score and updates the cache of top scores if necessary.
     *
     * @param playerId   The ID of the player.
     * @param playerName The name of the player.
     * @param score      The score achieved by the player.
     * @param timestamp  The timestamp indicating when the score was achieved.
     */


    public synchronized void updateDatabase(int playerId, String playerName, int score, LocalDateTime timestamp) throws UpdatingDBException {

        logger.info("DB is being updated by thread " +Thread.currentThread().getId() + " name " +Thread.currentThread().getName());
        Player player = new Player(playerId,playerName);
        PlayerScore playerScore = new PlayerScore(player,score,timestamp);
        try{

            playerScoreRepository.save(playerScore);
            logger.info("************* Successfully updated Player score in database ********");
        }
        catch (DataAccessException e)
        {
            throw new UpdatingDBException("Error occurred while Updating to database or cache " ,e);
        }

    }

    /**
     * Counts the total number of lines in the file specified by the given file path.
     *
     * @param file The path of the file to count the lines in.
     * @return The total number of lines in the file.
     * @throws FileCountLineException If an I/O error occurs while reading the file.
     */

    public int countTotalLines(File file) throws FileCountLineException {
        try
        {
            Path filePath = file.toPath();
            long lineCount = Files.lines(filePath).count();
            logger.info("******* Returning Total no. of lines in file ************");
            return (int) lineCount;
        }
        catch (IOException e)
        {
            throw new FileCountLineException("Exception Occurred while Counting Lines in File ",e);
        }
    }

    /**
     * Retrieves the top 5 player scores from the cache.
     *
     * This method retrieves the top 5 player scores from the cache and returns them
     * in a list sorted in descending order of score.
     *
     * @return A list containing the top 5 player scores. The scores are sorted in descending
     *         order of score.
     */


    public List<PlayerScore> getTopScores() {

        List<PlayerScore> top5 = new ArrayList<>(cache);

        top5.sort(new PlayerScoreComparator());
        return top5;
    }

    /**
     * Retrieves the top 5 scores from the database.
     *
     * @return A list of PlayerScore objects representing the top 5 scores.
     */
    public List<PlayerScore> retrieveTopKScoresFromDatabase() {

        logger.info("********* Fetching Top5 scores from Database. ***********");
        return playerScoreRepository.findTopkByOrderByScoreDesc(Constant.TOP_PLAYERS);
    }

    /**
     * Executes multiple tasks concurrently, updating the database and cache.
     * This method creates a new player and player score based on the provided parameters,
     * and then submits two tasks to the executor service to update the database and cache
     * concurrently.
     *
     * @param playerId   The ID of the player.
     * @param playerName The name of the player.
     * @param score      The score of the player.
     * @param timestamp  The timestamp of the score.
     * @throws RuntimeException If an exception occurs during the database update or
     *                          cache update process. The original exception, which can
     *                          be either UpdatingDBException or UpdatingCacheException,
     *                          is wrapped and rethrown as a RuntimeException.
     */
    public void executeMultipleTask(int playerId, String playerName, int score, LocalDateTime timestamp)
    {
        Player player = new Player(playerId,playerName);
        PlayerScore playerScore = new PlayerScore(player,score,timestamp);

        executorService.submit(() -> {
            try {
                updateDatabase(playerId, playerName, score, timestamp);
            } catch (UpdatingDBException e) {
                throw new RuntimeException(e);
            }
        });
        executorService.submit(() -> {
            try {
                updateTopScoresInCache(playerScore);
            } catch (UpdatingCacheException e) {
                throw new RuntimeException(e);
            }
        });


    }
}
