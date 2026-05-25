package com.accesosport.image.domain.exception;

public class InvalidImageException extends RuntimeException {

    public InvalidImageException(String messageKey) {
        super(messageKey);
    }
}
