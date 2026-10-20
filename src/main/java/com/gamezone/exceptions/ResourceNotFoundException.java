package com.gamezone.exceptions;

public class ResourceNotFoundException extends GameZoneException {

    public ResourceNotFoundException(String message) {
        super(message, "404");
    }

    public ResourceNotFoundException(String resourceType, String id) {
        super("No se encontró el recurso " + resourceType + " con ID: " + id, "404");
    }
}