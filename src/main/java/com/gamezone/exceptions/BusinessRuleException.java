package com.gamezone.exceptions;

public class BusinessRuleException extends GameZoneException {
    public BusinessRuleException(String message) {
        super(message, 400);
    }
}