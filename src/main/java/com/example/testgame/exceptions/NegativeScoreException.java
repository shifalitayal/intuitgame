package com.example.testgame.exceptions;

public class NegativeScoreException extends Exception{
    public NegativeScoreException(String errorMessage) {
        super(errorMessage);
    }
}
