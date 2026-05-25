package com.accesosport.user.domain.repository;

import com.accesosport.user.domain.model.UserParticipantProfile;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository port for domain actions of the Participant Profile
 */
public interface ParticipantProfileRepository {

    /**
     * Finds the Participant Profile of the given user by its userId
     *
     * @param userId The user to find its profile
     * @return the User Participant Profile
     */
    Optional<UserParticipantProfile> findByUserId(UUID userId);

    /**
     * Persists a Participant Profile
     *
     * @param profile The User Participant Profile
     * @return the persisted User Participant Profile
     */
    UserParticipantProfile save(UserParticipantProfile profile);
}
