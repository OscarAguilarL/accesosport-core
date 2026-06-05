package com.accesosport.auth.domain.exception;

public class TokenAlreadyUsedException extends RuntimeException {
    
    public TokenAlreadyUsedException(String messageKey) {
        super(messageKey);
    }
}
