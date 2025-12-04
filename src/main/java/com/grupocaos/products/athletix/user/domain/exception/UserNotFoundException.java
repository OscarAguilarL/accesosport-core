package com.grupocaos.products.athletix.user.domain.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String messageKey) {
        super(messageKey);
    }
}
