package com.accesosport.registration.application.dto;

import java.util.UUID;

public record RegisterParticipantCommand(UUID eventId, UUID participantId, UUID modalityId) {}
