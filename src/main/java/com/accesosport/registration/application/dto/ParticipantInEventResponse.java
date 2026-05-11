package com.accesosport.registration.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ParticipantInEventResponse(
        UUID registrationId,
        UUID participantId,
        String fullName,
        String email,
        String shirtSize,
        String bloodType,
        String medicalConditions,
        String emergencyContactName,
        String emergencyContactPhone,
        String status,
        String ticketCode,
        Integer bibNumber,
        boolean kitPickedUp,
        LocalDateTime kitPickedUpAt,
        LocalDateTime registeredAt
) {}
