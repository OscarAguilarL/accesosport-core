package com.accesosport.registration.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class CheckinToken {

    private UUID id;
    private UUID eventId;
    private UUID generatedByOrganizerId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;

    private CheckinToken() {
    }

    public static CheckinToken generate(UUID eventId, UUID organizerId, int validHours) {
        CheckinToken t = new CheckinToken();
        t.id = UUID.randomUUID();
        t.eventId = eventId;
        t.generatedByOrganizerId = organizerId;
        t.token = UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        t.expiresAt = LocalDateTime.now().plusHours(validHours);
        t.createdAt = LocalDateTime.now();
        return t;
    }

    public static CheckinToken reconstitute(UUID id, UUID eventId, UUID generatedByOrganizerId,
                                             String token, LocalDateTime expiresAt, LocalDateTime createdAt) {
        CheckinToken t = new CheckinToken();
        t.id = id;
        t.eventId = eventId;
        t.generatedByOrganizerId = generatedByOrganizerId;
        t.token = token;
        t.expiresAt = expiresAt;
        t.createdAt = createdAt;
        return t;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
