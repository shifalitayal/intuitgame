package com.example.testgame.exceptions;

public class ProcessingFileScoreException extends Exception{

    public ProcessingFileScoreException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
