package com.gamezone.exceptions;

public class BusinessRuleException extends GameZoneException {

    public BusinessRuleException(String message) {
        super(message, "400");
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode);
    }
}