package com.gamezone.exceptions;

public class InvalidDataException extends GameZoneException {

    public InvalidDataException(String message) {
        super(message, "422");
    }

    public InvalidDataException(String fieldName, String reason) {
        super("El campo " + fieldName + " es inválido: " + reason, "422");
    }
}
