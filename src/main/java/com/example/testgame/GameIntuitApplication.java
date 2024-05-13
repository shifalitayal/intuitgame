package com.example.testgame;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

import java.io.File;

import static com.example.testgame.constants.Constant.DIRECTORY_PATH;


@SpringBootApplication
@EnableScheduling
//@EnableRedisRepositories
public class GameIntuitApplication {

	public static void main(String[] args) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("Application is shutting down. Deleting files...");
			deleteFilesInDirectory(DIRECTORY_PATH);
		}));

		SpringApplication.run(GameIntuitApplication.class, args);
	}

	private static void deleteFilesInDirectory(String directoryPath) {
		File directory = new File(directoryPath);
		if (directory.exists() && directory.isDirectory()) {
			File[] files = directory.listFiles();
			if (files != null) {
				for (File file : files) {
					if (file.isFile()) {
						if (!file.delete()) {
							System.err.println("Failed to delete file: " + file.getAbsolutePath());
						} else {
							System.out.println("Deleted file: " + file.getAbsolutePath());
						}
					}
				}
			}
		}
	}


}
