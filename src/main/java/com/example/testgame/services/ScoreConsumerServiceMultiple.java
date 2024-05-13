//package com.example.testgame.services;
//
//import com.example.testgame.constants.Constant;
//import com.example.testgame.models.Player;
//import com.example.testgame.models.PlayerScore;
//import com.example.testgame.repositories.PlayerScoreRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.util.*;
//import java.util.logging.Logger;
//import java.util.stream.IntStream;
//
//@Component
//public class ScoreConsumerServiceMultiple implements IScoreConsumer {
//
//    static final Logger logger = Logger.getLogger(ScoreConsumerServiceMultiple.class.getName());
//    public List<PlayerScore> cache =new ArrayList<>();
//
//
//    private int lastProcessedLineNumber = 0;
//
//    private final String directoryPath = "src\\main\\resources\\files\\";
//    private final Map<String, Integer> lastProcessedLineMap = new HashMap<>();
//
//    @Autowired
//    private PlayerScoreRepository playerScoreRepository;
//
//    /**
//     * Run after every 10s to process/consume scores from file
//     * To add only the newly added entries from the file to the database, It keeps track of the last processed line
//     * number in the file.
//     * During each execution of the scheduled task, we count the total number of lines in the file.
//     * If the lastProcessedLineNumber is less than the total number of lines in the file, it indicates that there
//     * are new entries to be processed.
//     * We then process only the new entries from the file starting from the line after the lastProcessedLineNumber
//     * up to the last line in the file.
//     */
//    @Scheduled(fixedRate = 10000)
//    public void processScoresFromFile() throws IOException {
//        try {
//
//            File directory = new File(directoryPath);
//            File[] files = directory.listFiles();
//
//            if (files != null) {
//                for (File file : files) {
//                    if (file.isFile()) {
//                        int totalLines = countTotalLines(file);
//                        int lastProcessedLine = lastProcessedLineMap.getOrDefault(file.getName(), 0);
//                        if (lastProcessedLine < totalLines) {
//                            logger.info("***** Processing new entries ******");
//                            processNewScoresFromFile(file, lastProcessedLine + 1, totalLines);
//                        }
//                    }
//                }
//            }
//
//        } catch (IOException e) {
//            String errorMessage = "Error occurred while processing scores from file: " + e.getMessage();
//            logger.severe(errorMessage);
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Processes player scores from the file within the specified line range.
//     *
//     * @param startLine The line number from which to start processing (inclusive).
//     * @param endLine   The line number at which to end processing (exclusive).
//     *                  If endLine is greater than the total number of lines in the file,
//     *                  processing will continue until the end of the file is reached.
//     */
//
//    public void processNewScoresFromFile(File file,int startLine, int endLine) {
//        logger.info("file ****** "+file + " startline " +startLine + " end "+ endLine);
//        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
//            String line;
//            int lineNumber = 0;
//            logger.info("********* Consuming from file **********");
//            while ((line = reader.readLine()) != null && lineNumber < endLine) {
//                logger.info("line "+line);
//                lineNumber++;
//                if (lineNumber >= startLine) {
//                    String[] parts = line.split(",");
//                    logger.info("parts *****" +parts[0]);
//                    int playerId = Integer.parseInt(parts[0]);
//                    String playerName = parts[1];
//                    int score = Integer.parseInt(parts[2]);
//                    LocalDateTime timestamp = LocalDateTime.parse(parts[3]);
//                    logger.info("********** Updating Database ******");
//                    updateDatabaseAndCache(playerId, playerName,score, timestamp);
//
//                }
//            }
//            lastProcessedLineMap.put(file.getName(), endLine);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    /**
//     * Updates the top scores cache with a new player score if it is higher than the current lowest score in the cache.
//     *
//     * @param playerScore The player score to be considered for updating the cache.
//     */
//
//    public void updateTopScoresInCache(PlayerScore playerScore) {
//        if(cache.isEmpty() || cache.size()<5)
//        {
//            cache = retrieveTop5ScoresFromDatabase();
//            logger.info("cache size "+cache.size());
//        }
//        else{
//            int lowestIndex = findLowestIndex();
//
//            PlayerScore lowestPlayerScore = cache.get(lowestIndex);
//
//            if (playerScore.getScore() > lowestPlayerScore.getScore()) {
//                cache.set(lowestIndex, playerScore);
//            }
//            logger.info("************ Successfully Updated Cache ***********");
//        }
//
//    }
//
//
//    /**
//     * Finds the index of the player score with the lowest score in the cache.
//     *
//     * @return The index of the player score with the lowest score in the cache.
//     */
//    private int findLowestIndex() {
//
//        logger.info("***** Finding index of Lowest element **********");
//        int latestIndex = IntStream.range(0, cache.size())
//                .reduce((i, j) -> {
//                    int compare = Integer.compare(cache.get(i).getScore(), cache.get(j).getScore());
//                    if (compare != 0) {
//                        return compare < 0 ? i : j;
//                    } else {
//                        // If scores are equal, compare by LocalDateTime
//                        return cache.get(i).getTimestamp().isAfter(cache.get(j).getTimestamp()) ? i : j;
//                    }
//                })
//                .orElse(-1);
//
//        logger.info("************ Found index of lowest element *********" +latestIndex);
//        return latestIndex;
//    }
//
//    /**
//     * Updates the database with a new player score and updates the cache of top scores if necessary.
//     *
//     * @param playerId   The ID of the player.
//     * @param playerName The name of the player.
//     * @param score      The score achieved by the player.
//     * @param timestamp  The timestamp indicating when the score was achieved.
//     */
//
//
//    void updateDatabaseAndCache(int playerId, String playerName, int score, LocalDateTime timestamp) {
//
//        Player player = new Player(playerId,playerName);
//        PlayerScore playerScore = new PlayerScore(player,score,timestamp);
//        try{
//            playerScoreRepository.save(playerScore);
//            logger.info("************* Successfully updated Player score in database ********");
//
//            logger.info("*********** Updating Cache size ****** "+cache.size());
//
//            updateTopScoresInCache(playerScore);
//
//        }
//        catch (Exception e)
//        {
//            String errorMessage = "Error occurred while Updating to database or cache: " + e.getMessage();
//            logger.severe(errorMessage);
//            e.printStackTrace();
//        }
//
//
//    }
//
//    /**
//     * Counts the total number of lines in the file specified by the given file path.
//     *
//     * @param file The path of the file to count the lines in.
//     * @return The total number of lines in the file.
//     * @throws IOException If an I/O error occurs while reading the file.
//     */
//
//    public int countTotalLines(File file) throws IOException {
//        Path filePath = file.toPath();
//        long lineCount = Files.lines(filePath).count();
//        logger.info("******* Returning Total no. of lines in file ************");
//        return (int) lineCount;
//    }
//
//
//    /**
//     * Retrieves the top scores from the cache.
//     *
//     * @return A list of PlayerScore objects representing the top scores.
//     */
//    public List<PlayerScore> getTopScores() {
//        Comparator<PlayerScore> scoreComparator = Comparator.comparingInt(PlayerScore::getScore).reversed();
//        cache.sort(scoreComparator);
//        return cache;
//    }
//
//    /**
//     * Retrieves the top 5 scores from the database.
//     *
//     * @return A list of PlayerScore objects representing the top 5 scores.
//     */
//
//
//    public List<PlayerScore> retrieveTop5ScoresFromDatabase() {
//
//        logger.info("********* Fetching Top5 scores from Database. ***********");
//        return playerScoreRepository.findTop5ByOrderByScoreDesc();
//    }
//}
