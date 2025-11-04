package com.grupocaos.products.athletix.event.domain.exception;

import java.util.UUID;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(UUID id) {
        super(String.format("Event with id %s not found", id));
    }

    public EventNotFoundException(String message) {
        super(message);
    }
}
