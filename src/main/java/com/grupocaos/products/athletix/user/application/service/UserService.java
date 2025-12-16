package com.grupocaos.products.athletix.user.application.service;

import com.grupocaos.products.athletix.user.application.dto.CreateOrganizerProfileRequest;
import com.grupocaos.products.athletix.user.application.dto.CreateParticipantProfileRequest;
import com.grupocaos.products.athletix.user.application.dto.OrganizerProfileResponse;
import com.grupocaos.products.athletix.user.application.dto.ParticipantProfileResponse;
import com.grupocaos.products.athletix.user.domain.exception.ProfileNotFoundException;
import com.grupocaos.products.athletix.user.domain.repository.OrganizerProfileRepository;
import com.grupocaos.products.athletix.user.domain.repository.ParticipantProfileRepository;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import com.grupocaos.products.athletix.user.domain.usecase.CreateOrganizerProfileUseCase;
import com.grupocaos.products.athletix.user.domain.usecase.CreateParticipantProfileUseCase;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service class responsible for managing user profiles, including
 * organizer and participant profiles. Handles operations such as profile
 * creation and retrieval.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final ParticipantProfileRepository participantProfileRepository;

    /**
     * Creates an organizer profile associated with the specified user ID.
     *
     * @param userId  the unique identifier of the user for whom the organizer profile is to be created
     * @param request the data transfer object containing the details required to create the organizer profile
     * @return an {@code OrganizerProfileResponse} containing the details of the newly created organizer profile
     */
    @Transactional
    public OrganizerProfileResponse createOrganizerProfile(UUID userId, CreateOrganizerProfileRequest request) {
        var command = new CreateOrganizerProfileUseCase.Command(
                request.organizationName(),
                request.contactName(),
                request.phone(),
                request.address().toDomain(),
                request.website(),
                request.facebook(),
                request.instagram(),
                request.description(),
                userId
        );
        CreateOrganizerProfileUseCase useCase = new CreateOrganizerProfileUseCase(organizerProfileRepository, userRepository, roleRepository);
        var result = useCase.execute(command);
        return OrganizerProfileResponse.fromDomain(result.profile());
    }

    /**
     * Creates a participant profile associated with the specified user ID.
     *
     * @param userId  the unique identifier of the user for whom the participant profile is to be created
     * @param request the data transfer object containing the details required to create the participant profile
     * @return a {@code ParticipantProfileResponse} containing the details of the newly created participant profile
     */
    public ParticipantProfileResponse createParticipantProfile(UUID userId, CreateParticipantProfileRequest request) {
        var command = new CreateParticipantProfileUseCase.Command(
                request.firstName(),
                request.lastName(),
                request.secondLastName(),
                request.birthDate(),
                request.gender(),
                request.phoneNumber(),
                request.address().toDomain(),
                request.shirtSize(),
                request.emergencyContactName(),
                request.emergencyContactPhone(),
                request.medicalConditions(),
                request.bloodType(),
                userId
        );
        CreateParticipantProfileUseCase useCase = new CreateParticipantProfileUseCase(participantProfileRepository, userRepository, roleRepository);
        var result = useCase.execute(command);
        return ParticipantProfileResponse.fromDomain(result.profile());
    }

    /**
     * Retrieves the organizer profile for the given user ID.
     *
     * @param userId the unique identifier of the user whose organizer profile is to be retrieved
     * @return the organizer profile response containing the details of the user's organizer profile
     * @throws ProfileNotFoundException if no organizer profile is found for the given user ID
     */
    public OrganizerProfileResponse getOrganizerProfile(UUID userId) {
        var profile = organizerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Organizer profile not found"));
        return OrganizerProfileResponse.fromDomain(profile);
    }

    /**
     * Retrieves the participant profile for the given user ID.
     *
     * @param userId the unique identifier of the user whose participant profile is to be retrieved
     * @return the participant profile response containing the details of the user's participant profile
     * @throws ProfileNotFoundException if no participant profile is found for the given user ID
     */
    public ParticipantProfileResponse getParticipantProfile(UUID userId) {
        var profile = participantProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Participant profile not found"));
        return ParticipantProfileResponse.fromDomain(profile);
    }
}
