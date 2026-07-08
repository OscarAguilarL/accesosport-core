package com.accesosport.registration.application.dto;

import com.accesosport.registration.domain.model.Registration;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistrationResponse(
        UUID id,
        UUID eventId,
        String eventName,
        LocalDateTime eventDate,
        String status,
        String ticketCode,
        Integer bibNumber,
        boolean kitPickedUp,
        LocalDateTime registeredAt,
        String paymentAccessToken  // only populated once, at anonymous registration time
) {
    public static RegistrationResponse from(Registration r) {
        return new RegistrationResponse(
                r.getId(),
                r.getEventId(),
                null,
                null,
                r.getStatus().name(),
                r.getTicketCode(),
                r.getBibNumber(),
                r.isKitPickedUp(),
                r.getRegisteredAt(),
                null
        );
    }

    public static RegistrationResponse from(Registration r, String eventName, LocalDateTime eventDate) {
        return new RegistrationResponse(
                r.getId(),
                r.getEventId(),
                eventName,
                eventDate,
                r.getStatus().name(),
                r.getTicketCode(),
                r.getBibNumber(),
                r.isKitPickedUp(),
                r.getRegisteredAt(),
                null
        );
    }

    public static RegistrationResponse fromWithToken(Registration r, String plainTextToken) {
        return new RegistrationResponse(
                r.getId(),
                r.getEventId(),
                null,
                null,
                r.getStatus().name(),
                r.getTicketCode(),
                r.getBibNumber(),
                r.isKitPickedUp(),
                r.getRegisteredAt(),
                plainTextToken
        );
    }
}
