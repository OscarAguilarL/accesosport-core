package com.accesosport.user.domain.repository;

import com.accesosport.user.domain.model.UserOrganizerProfile;

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
     * Finds an Organizer Profile by its associated Stripe account ID.
     *
     * @param stripeAccountId the Stripe account ID to search for
     * @return the matching UserOrganizerProfile, or empty if not found
     */
    Optional<UserOrganizerProfile> findByStripeAccountId(String stripeAccountId);

    /**
     * Persists a Participant Profile
     *
     * @param profile The User Organizer Profile
     * @return the persisted User Organizer Profile
     */
    UserOrganizerProfile save(UserOrganizerProfile profile);
}
