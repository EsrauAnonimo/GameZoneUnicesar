package com.gamezone.exceptions;

/**
 * Base exception for all custom exceptions in the GameZone system.
 * Extends RuntimeException to avoid checked exceptions.
 * 
 * @author Dev 1
 */
public class GameZoneException extends RuntimeException {

    private int errorCode;

    public GameZoneException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
