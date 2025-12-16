package com.grupocaos.products.athletix.user.infrastructure.persistence.adapter;

import com.grupocaos.products.athletix.user.domain.model.UserOrganizerProfile;
import com.grupocaos.products.athletix.user.domain.repository.OrganizerProfileRepository;
import com.grupocaos.products.athletix.user.infrastructure.persistence.jpa.OrganizerProfileJpaRepository;
import com.grupocaos.products.athletix.user.infrastructure.persistence.mapper.OrganizerProfileMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementation of the {@link OrganizerProfileRepository} interface
 * that provides a connection between the domain layer and the persistence layer.
 * This class handles the operations related to the Organizer Profile entity by
 * leveraging the {@link OrganizerProfileJpaRepository}.
 * <p>
 * It encapsulates the interaction with the underlying JPA repository,
 * translating between domain models ({@code UserOrganizerProfile}) and any
 * persistent representations, if needed, to ensure proper communication between
 * layers.
 * <p>
 * This class is annotated with {@code @Repository} to indicate it's a Spring
 * repository component responsible for data access logic and is autowired into
 * dependent beans. It is also annotated with {@code @AllArgsConstructor} to
 * generate constructors for its final fields.
 */
@Repository
@AllArgsConstructor
public class OrganizerProfileRepositoryAdapter implements OrganizerProfileRepository {

    private final OrganizerProfileJpaRepository organizerProfileJpaRepository;

    @Override
    public Optional<UserOrganizerProfile> findByUserId(UUID userId) {
        return organizerProfileJpaRepository.findByUserId(userId)
                .map(OrganizerProfileMapper::toDomain);
    }

    @Override
    public UserOrganizerProfile save(UserOrganizerProfile profile) {
        var entity = OrganizerProfileMapper.toEntity(profile);
        var savedEntity = organizerProfileJpaRepository.save(entity);
        return OrganizerProfileMapper.toDomain(savedEntity);
    }
}
