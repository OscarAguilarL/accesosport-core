package com.accesosport.invitation.infrastructure.persistence.mapper;

import com.accesosport.invitation.domain.model.InvitationStatus;
import com.accesosport.invitation.domain.model.InvitationToken;
import com.accesosport.invitation.infrastructure.persistence.entity.InvitationTokenJpaEntity;

public class InvitationTokenMapper {

    private InvitationTokenMapper() {}

    public static InvitationToken toDomain(InvitationTokenJpaEntity entity) {
        return InvitationToken.reconstitute(
                entity.getId(),
                entity.getToken(),
                entity.getEmail(),
                entity.getReason(),
                InvitationStatus.valueOf(entity.getStatus()),
                entity.getCreatedBy(),
                entity.getCreatedAt(),
                entity.getUsedAt(),
                entity.getUsedByUserId(),
                entity.getRevokedAt()
        );
    }

    public static InvitationTokenJpaEntity toEntity(InvitationToken domain) {
        return new InvitationTokenJpaEntity(
                domain.getId(),
                domain.getToken(),
                domain.getEmail(),
                domain.getReason(),
                domain.getStatus().name(),
                domain.getCreatedBy(),
                domain.getCreatedAt(),
                domain.getUsedAt(),
                domain.getUsedByUserId(),
                domain.getRevokedAt()
        );
    }
}
