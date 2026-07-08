package com.accesosport.invitation.application.dto;

import com.accesosport.invitation.domain.model.InvitationToken;

import java.time.LocalDateTime;
import java.util.UUID;

public record InvitationResponse(
        UUID id,
        UUID token,
        String email,
        String reason,
        String status,
        LocalDateTime createdAt,
        LocalDateTime usedAt,
        LocalDateTime revokedAt
) {
    public static InvitationResponse fromDomain(InvitationToken t) {
        return new InvitationResponse(
                t.getId(),
                t.getToken(),
                t.getEmail(),
                t.getReason(),
                t.getStatus().name(),
                t.getCreatedAt(),
                t.getUsedAt(),
                t.getRevokedAt()
        );
    }
}
