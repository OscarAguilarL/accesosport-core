package com.accesosport.invitation.domain.model;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class InvitationToken {

    private UUID id;
    private UUID token;
    private String email;
    private String reason;
    private InvitationStatus status;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime usedAt;
    private UUID usedByUserId;
    private LocalDateTime revokedAt;

    private InvitationToken() {}

    public static InvitationToken create(String email, String reason, UUID createdBy) {
        InvitationToken inv = new InvitationToken();
        inv.id = UUID.randomUUID();
        inv.token = UUID.randomUUID();
        inv.email = email.toLowerCase().trim();
        inv.reason = reason;
        inv.status = InvitationStatus.PENDING;
        inv.createdBy = createdBy;
        inv.createdAt = LocalDateTime.now();
        return inv;
    }

    public static InvitationToken reconstitute(UUID id, UUID token, String email, String reason,
                                               InvitationStatus status, UUID createdBy,
                                               LocalDateTime createdAt, LocalDateTime usedAt,
                                               UUID usedByUserId, LocalDateTime revokedAt) {
        InvitationToken inv = new InvitationToken();
        inv.id = id;
        inv.token = token;
        inv.email = email;
        inv.reason = reason;
        inv.status = status;
        inv.createdBy = createdBy;
        inv.createdAt = createdAt;
        inv.usedAt = usedAt;
        inv.usedByUserId = usedByUserId;
        inv.revokedAt = revokedAt;
        return inv;
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public void markAsUsed(UUID userId) {
        if (!isPending()) throw new IllegalStateException("Solo se pueden usar invitaciones PENDING");
        this.status = InvitationStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.usedByUserId = userId;
    }

    public void revoke() {
        if (!isPending()) throw new IllegalStateException("Solo se pueden revocar invitaciones PENDING");
        this.status = InvitationStatus.REVOKED;
        this.revokedAt = LocalDateTime.now();
    }
}
