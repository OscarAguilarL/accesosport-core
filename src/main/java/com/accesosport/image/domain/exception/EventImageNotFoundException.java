package com.accesosport.image.domain.exception;

import java.util.UUID;

public class EventImageNotFoundException extends RuntimeException {

    public EventImageNotFoundException(UUID imageId) {
        super("image.errors.not-found");
    }
}
