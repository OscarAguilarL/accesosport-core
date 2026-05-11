package com.accesosport.event.domain.exception;

import java.util.UUID;

public class ModalityHasRegistrationsException extends RuntimeException {
    public ModalityHasRegistrationsException(UUID modalityId) {
        super("Cannot delete modality " + modalityId + " because it already has registrations");
    }
}
