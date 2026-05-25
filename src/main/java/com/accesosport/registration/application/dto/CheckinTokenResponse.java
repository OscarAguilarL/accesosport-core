package com.accesosport.registration.application.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CheckinTokenResponse(
        String token,
        UUID eventId,
        LocalDateTime expiresAt
) {}
