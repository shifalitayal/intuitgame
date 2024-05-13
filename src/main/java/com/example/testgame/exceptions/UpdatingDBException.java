package com.example.testgame.exceptions;

public class UpdatingDBException extends Exception{
    public UpdatingDBException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
}
