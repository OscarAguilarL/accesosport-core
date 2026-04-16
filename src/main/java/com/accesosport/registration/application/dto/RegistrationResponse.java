package com.accesosport.registration.application.dto;

import com.accesosport.registration.domain.model.Registration;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistrationResponse(
        UUID registrationId,
        UUID eventId,
        String status,
        String ticketCode,
        Integer bibNumber,
        boolean kitPickedUp,
        LocalDateTime registeredAt
) {
    public static RegistrationResponse from(Registration r) {
        return new RegistrationResponse(
                r.getId(),
                r.getEventId(),
                r.getStatus().name(),
                r.getTicketCode(),
                r.getBibNumber(),
                r.isKitPickedUp(),
                r.getRegisteredAt()
        );
    }
}
