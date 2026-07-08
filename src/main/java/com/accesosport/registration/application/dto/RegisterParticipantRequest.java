package com.accesosport.registration.application.dto;

import java.util.UUID;

public record RegisterParticipantRequest(
        String participantEmail,
        String participantFirstName,
        String participantLastName,
        String participantPhone,
        UUID modalityId,
        UUID categoryId,
        boolean waiverAccepted,
        Boolean wantsShirt,
        String shirtSize,
        String bloodType,
        String emergencyContactName,
        String emergencyContactPhone,
        String medicalConditions
) {}
