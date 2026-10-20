package com.gamezone.exceptions;

/**
 * Base exception for all custom exceptions in the GameZone system.
 * Extends RuntimeException to avoid checked exceptions.
 */
public class GameZoneException extends RuntimeException {

    private String errorCode;

    public GameZoneException(String message) {
        super(message);
        this.errorCode = "500";
    }

    public GameZoneException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public GameZoneException(String message, int errorCode) {
        super(message);
        this.errorCode = String.valueOf(errorCode);
    }

    public GameZoneException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "500";
    }

    public GameZoneException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
