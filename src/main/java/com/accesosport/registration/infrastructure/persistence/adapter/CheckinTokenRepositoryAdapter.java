package com.accesosport.registration.infrastructure.persistence.adapter;

import com.accesosport.registration.domain.model.CheckinToken;
import com.accesosport.registration.domain.repository.CheckinTokenRepository;
import com.accesosport.registration.infrastructure.persistence.entity.CheckinTokenJpaEntity;
import com.accesosport.registration.infrastructure.persistence.jpa.CheckinTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CheckinTokenRepositoryAdapter implements CheckinTokenRepository {

    private final CheckinTokenJpaRepository jpaRepository;

    @Override
    public CheckinToken save(CheckinToken token) {
        CheckinTokenJpaEntity entity = toEntity(token);
        CheckinTokenJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<CheckinToken> findByToken(String token) {
        return jpaRepository.findByToken(token).map(this::toDomain);
    }

    private CheckinTokenJpaEntity toEntity(CheckinToken t) {
        return new CheckinTokenJpaEntity(
                t.getId(),
                t.getEventId(),
                t.getGeneratedByOrganizerId(),
                t.getToken(),
                t.getExpiresAt(),
                t.getCreatedAt()
        );
    }

    private CheckinToken toDomain(CheckinTokenJpaEntity e) {
        return CheckinToken.reconstitute(
                e.getId(),
                e.getEventId(),
                e.getGeneratedByOrganizerId(),
                e.getToken(),
                e.getExpiresAt(),
                e.getCreatedAt()
        );
    }
}
