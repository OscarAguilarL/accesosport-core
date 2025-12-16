package com.grupocaos.products.athletix.user.infrastructure.persistence.jpa;

import com.grupocaos.products.athletix.user.infrastructure.persistence.entity.UserJpaEntity;
import com.grupocaos.products.athletix.user.infrastructure.persistence.entity.UserOrganizerProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

/**
 * Represents the JPA repository interface for managing {@code UserOrganizerProfileJpaEntity}.
 * Provides methods for executing CRUD operations and custom queries on the organizer profile data.
 * Extends {@code JpaRepository} to leverage Spring Data JPA functionalities.
 */
public interface OrganizerProfileJpaRepository extends JpaRepository<UserOrganizerProfileJpaEntity, UUID> {

    /**
     * Retrieves an optional user organizer profile based on the provided user ID.
     *
     * @param userId the unique identifier of the user whose organizer profile is to be retrieved.
     *               Cannot be null.
     * @return an {@code Optional} containing the {@code UserOrganizerProfileJpaEntity} associated
     * with the specified user ID, if found; otherwise, an empty {@code Optional}.
     */
    @Query("SELECT up FROM UserOrganizerProfileJpaEntity up WHERE up.user.id = :userId")
    Optional<UserOrganizerProfileJpaEntity> findByUserId(UUID userId);
}
