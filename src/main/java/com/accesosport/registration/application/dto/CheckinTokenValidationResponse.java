package com.accesosport.registration.application.dto;

import java.util.UUID;

public record CheckinTokenValidationResponse(
        boolean valid,
        UUID eventId,
        String eventName
) {}
