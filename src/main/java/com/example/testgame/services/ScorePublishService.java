//package com.example.testgame.services;
//
//import com.example.testgame.constants.Constant;
//import com.example.testgame.models.Player;
//import com.example.testgame.repositories.PlayerRepository;
//import com.example.testgame.repositories.PlayerScoreRepository;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import java.io.FileWriter;
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Random;
//import java.util.logging.Logger;
//
//@Service
//public class ScorePublishService implements IScorePublisher{
//
//    private static final Logger logger = Logger.getLogger(ScorePublishService.class.getName());
//
//    private final String directoryPath = "src\\main\\resources\\files\\";
//    @Autowired
//    private PlayerRepository playerRepository;
//
//    @Autowired
//    private PlayerScoreRepository playerScoreRepository;
//
//    public ScorePublishService(){}
//
//    @PostConstruct
//    public void init() {
//        publishPlayerScores();
//    }
//
//    /**
//     * Generates a random player from the list of players retrieved from the database.
//     *
//     * @return A randomly selected Player object from the list of players, or null if the list is empty.
//     */
//
//    public Player generateRandomPlayerName() {
//
//        List<Player> playerList = playerRepository.findAll();
//        Random random = new Random();
//
//        if (!playerList.isEmpty()) {
//
//            int randomIndex = random.nextInt(playerList.size());
//            logger.info("*************Player Joined the Game.**********");
//            return playerList.get(randomIndex);
//
//        } else {
//            return null;
//        }
//    }
//
//    /**
//     * Generates a random score between 0 and 100 (inclusive).
//     *
//     * @return A randomly generated integer score.
//     */
//    public int generateRandomScore() {
//        logger.info("***** Player Completed his game **********");
//        return (int) (Math.random() * 101);
//    }
//
//    /**
//     *  it's a scheduled task that runs every 15 seconds
//     * Generates random player scores and writes them to a file.
//     * This method is invoked periodically by a scheduled task.
//     */
//
//    @Scheduled(fixedRate = 15000) // Run every 1 second
//    public void publishPlayerScores() {
//        long startTime = System.currentTimeMillis();
//        Player player = generateRandomPlayerName();
//        assert player != null;
//        Integer playerId= player.getPlayerId();
//        String playerName = player.getPlayerName();
//        int score = generateRandomScore();
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
//}
