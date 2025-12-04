package com.grupocaos.products.athletix.user.domain.exception;

public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String messageKey) {
        super(messageKey);
    }
}
