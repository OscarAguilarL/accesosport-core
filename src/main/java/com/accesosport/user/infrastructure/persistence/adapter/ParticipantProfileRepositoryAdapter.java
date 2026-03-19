package com.accesosport.user.infrastructure.persistence.adapter;

import com.accesosport.user.domain.model.UserParticipantProfile;
import com.accesosport.user.domain.repository.ParticipantProfileRepository;
import com.accesosport.user.infrastructure.persistence.jpa.ParticipantProfileJpaRepository;
import com.accesosport.user.infrastructure.persistence.mapper.ParticipantProfileMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementation of the {@link ParticipantProfileRepository} interface. This class acts
 * as a bridge between the domain layer and the persistence layer, mapping domain entities
 * to persistence entities and vice versa.
 * <p>
 * This adapter uses the {@link ParticipantProfileJpaRepository} to perform persistence
 * operations for participant profiles and provides domain-corresponding functionality
 * outlined in {@link ParticipantProfileRepository}.
 * <p>
 * It leverages the {@link ParticipantProfileMapper} for converting persistence entities to domain
 * entities and vice versa.
 * <p>
 * Dependencies required for this adapter are provided via constructor injection.
 */
@Repository
@AllArgsConstructor
public class ParticipantProfileRepositoryAdapter implements ParticipantProfileRepository {

    private final ParticipantProfileJpaRepository participantProfileJpaRepository;

    @Override
    public Optional<UserParticipantProfile> findByUserId(UUID userId) {
        return participantProfileJpaRepository.findByUserId(userId)
                .map(ParticipantProfileMapper::toDomain);
    }

    @Override
    public UserParticipantProfile save(UserParticipantProfile profile) {
        var entity = ParticipantProfileMapper.toEntity(profile);
        var savedEntity = participantProfileJpaRepository.save(entity);
        return ParticipantProfileMapper.toDomain(savedEntity);
    }
}
