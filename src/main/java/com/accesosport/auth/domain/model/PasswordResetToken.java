package com.accesosport.auth.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class PasswordResetToken {
    private UUID id;
    private UUID userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime usedAt;
    private LocalDateTime createdAt;

    private PasswordResetToken() {}

    public static PasswordResetToken create(UUID userId, String token, LocalDateTime expiresAt) {
        PasswordResetToken t = new PasswordResetToken();
        t.id = UUID.randomUUID();
        t.userId = userId;
        t.token = token;
        t.expiresAt = expiresAt;
        t.createdAt = LocalDateTime.now();
        return t;
    }

    public static PasswordResetToken reconstitute(UUID id, UUID userId, String token,
                                                   LocalDateTime expiresAt, LocalDateTime usedAt,
                                                   LocalDateTime createdAt) {
        PasswordResetToken t = new PasswordResetToken();
        t.id = id;
        t.userId = userId;
        t.token = token;
        t.expiresAt = expiresAt;
        t.usedAt = usedAt;
        t.createdAt = createdAt;
        return t;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isUsed() {
        return usedAt != null;
    }

    public void markAsUsed() {
        this.usedAt = LocalDateTime.now();
    }
}
