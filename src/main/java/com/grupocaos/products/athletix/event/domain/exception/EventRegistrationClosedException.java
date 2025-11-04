package com.grupocaos.products.athletix.event.domain.exception;

public class EventRegistrationClosedException extends RuntimeException {

    public EventRegistrationClosedException(String message) {
        super(message);
    }
}
