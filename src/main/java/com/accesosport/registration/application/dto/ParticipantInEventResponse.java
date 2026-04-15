package com.accesosport.registration.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantInEventResponse(
        UUID registrationId,
        UUID participantId,
        String status,
        String ticketCode,
        Integer bibNumber,
        boolean kitPickedUp,
        LocalDateTime registeredAt
) {}
