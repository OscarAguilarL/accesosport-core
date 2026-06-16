package com.accesosport.payment.application.service;

import com.accesosport.registration.domain.exception.RegistrationAccessDeniedException;
import com.accesosport.registration.domain.model.Registration;

import java.util.UUID;

public class RegistrationPaymentAccessService {

    public void assertCanAccess(Registration registration, UUID authenticatedUserId, String anonymousAccessToken) {
        if (registration.getParticipantId() != null) {
            if (!registration.getParticipantId().equals(authenticatedUserId)) {
                throw new RegistrationAccessDeniedException(registration.getId(), authenticatedUserId);
            }
        } else {
            if (!registration.verifyPaymentAccessToken(anonymousAccessToken)) {
                throw new RegistrationAccessDeniedException(registration.getId(), null);
            }
        }
    }
}
