package com.gamezone.exceptions;

public class PersistenceException extends GameZoneException {
    public PersistenceException(String message) {
        super(message, 500);
    }
}