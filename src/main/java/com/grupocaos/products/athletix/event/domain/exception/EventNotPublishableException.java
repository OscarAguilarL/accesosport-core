package com.grupocaos.products.athletix.event.domain.exception;

public class EventNotPublishableException extends RuntimeException {

    public EventNotPublishableException(String message) {
        super(message);
    }
}
