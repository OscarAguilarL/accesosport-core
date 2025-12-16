package com.grupocaos.products.athletix.user.domain.repository;

import com.grupocaos.products.athletix.user.domain.model.UserOrganizerProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * Port repository for domain actions of the Organizer Profile entity
 */
public interface OrganizerProfileRepository {

    /**
     * Finds the Organizer Profile of the given user by its userId
     *
     * @param userId The user to find its profile
     * @return the User Organizer profile
     */
    Optional<UserOrganizerProfile> findByUserId(UUID userId);

    /**
     * Persists a Participant Profile
     *
     * @param profile The User Organizer Profile
     * @return the persisted User Organizer Profile
     */
    UserOrganizerProfile save(UserOrganizerProfile profile);
}
