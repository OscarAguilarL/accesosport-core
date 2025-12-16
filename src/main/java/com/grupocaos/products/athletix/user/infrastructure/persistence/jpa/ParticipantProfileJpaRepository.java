package com.grupocaos.products.athletix.user.infrastructure.persistence.jpa;

import com.grupocaos.products.athletix.user.infrastructure.persistence.entity.UserParticipantProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for managing {@link UserParticipantProfileJpaEntity} persistence.
 * Extends {@link JpaRepository} to provide CRUD operations for participant profiles.
 * <p>
 * This interface includes custom query methods to facilitate specific data access patterns.
 */
public interface ParticipantProfileJpaRepository extends JpaRepository<UserParticipantProfileJpaEntity, UUID> {

    /**
     * Retrieves a user participant profile by the associated user ID.
     *
     * @param userId the unique identifier of the user whose participant profile is to be retrieved
     * @return an {@code Optional} containing the {@code UserParticipantProfileJpaEntity} if found, or an empty {@code Optional} if no profile exists for the given user ID
     */
    @Query("SELECT p FROM UserParticipantProfileJpaEntity p WHERE p.user.id = :userId")
    Optional<UserParticipantProfileJpaEntity> findByUserId(UUID userId);
}
