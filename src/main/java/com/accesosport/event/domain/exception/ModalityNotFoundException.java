package com.accesosport.event.domain.exception;

import java.util.UUID;

public class ModalityNotFoundException extends RuntimeException {
    public ModalityNotFoundException(UUID modalityId) {
        super("Modality not found: " + modalityId);
    }
}
