package com.gamezone.exceptions;

public class ResourceNotFoundException extends GameZoneException {
    public ResourceNotFoundException(String message) {
        super(message, 404);
    }
}