package com.accesosport.auth.domain.exception;

public class TokenExpiredException extends RuntimeException {

    public TokenExpiredException(String messageKey) {
        super(messageKey);
    }
}
