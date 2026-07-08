package com.accesosport.auth.domain.exception;

public class InvalidTokenException extends RuntimeException {

    public InvalidTokenException(String messageKey) {
        super(messageKey);
    }
}
