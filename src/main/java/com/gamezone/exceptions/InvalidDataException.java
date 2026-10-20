package com.gamezone.exceptions;

public class InvalidDataException extends GameZoneException {
    public InvalidDataException(String message) {
        super(message, 422);
    }
}
