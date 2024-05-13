package com.example.testgame.exceptions;

public class PublishingFileScoreException extends Exception {
    public PublishingFileScoreException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
