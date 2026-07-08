package com.accesosport.auth.infrastructure.persistence.mapper;

import com.accesosport.auth.domain.model.PasswordResetToken;
import com.accesosport.auth.infrastructure.persistence.entity.PasswordResetTokenJpaEntity;

public class PasswordResetTokenMapper {

    private PasswordResetTokenMapper() {}

    public static PasswordResetTokenJpaEntity toEntity(PasswordResetToken domain) {
        return new PasswordResetTokenJpaEntity(
                domain.getId(),
                domain.getUserId(),
                domain.getToken(),
                domain.getExpiresAt(),
                domain.getUsedAt(),
                domain.getCreatedAt()
        );
    }

    public static PasswordResetToken toDomain(PasswordResetTokenJpaEntity entity) {
        return PasswordResetToken.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getToken(),
                entity.getExpiresAt(),
                entity.getUsedAt(),
                entity.getCreatedAt()
        );
    }
}
