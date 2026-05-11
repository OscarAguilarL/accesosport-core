package com.accesosport.registration.domain.exception;

import com.accesosport.shared.domain.i18n.MessageKeys;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RegistrationNotConfirmedException extends RuntimeException {

    private final Object[] args;

    public RegistrationNotConfirmedException(UUID registrationId) {
        super(MessageKeys.Registrations.REGISTRATION_NOT_CONFIRMED);
        this.args = new Object[]{registrationId};
    }
}
