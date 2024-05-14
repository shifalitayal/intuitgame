package com.example.testgame.exceptions;

public class NegativePlayerIdOrPlayerNullException extends Exception{
    public NegativePlayerIdOrPlayerNullException(String errorMessage) {
        super(errorMessage);
    }
}
