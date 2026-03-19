package com.accesosport.user.application.service;

import com.accesosport.shared.application.dto.AddressDto;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.user.application.dto.CreateOrganizerProfileRequest;
import com.accesosport.user.application.dto.CreateParticipantProfileRequest;
import com.accesosport.user.application.dto.OrganizerProfileResponse;
import com.accesosport.user.application.dto.ParticipantProfileResponse;
import com.accesosport.user.application.dto.SavePersonalDataRequest;
import com.accesosport.user.application.dto.SaveUserAddressRequest;
import com.accesosport.user.application.dto.UserInformationDto;
import com.accesosport.user.domain.exception.ProfileNotFoundException;
import com.accesosport.user.domain.exception.UserNotFoundException;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import com.accesosport.user.domain.repository.ParticipantProfileRepository;
import com.accesosport.user.domain.repository.RoleRepository;
import com.accesosport.user.domain.repository.UserRepository;
import com.accesosport.user.domain.usecase.CreateOrganizerProfileUseCase;
import com.accesosport.user.domain.usecase.CreateParticipantProfileUseCase;
import com.accesosport.user.domain.usecase.SaveUserAddressUseCase;
import com.accesosport.user.domain.usecase.SaveUserPersonalInfoUseCase;
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
    @Transactional
    public ParticipantProfileResponse createParticipantProfile(UUID userId, CreateParticipantProfileRequest request) {
        var command = new CreateParticipantProfileUseCase.Command(
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
                .orElseThrow(() -> new ProfileNotFoundException(MessageKeys.Users.USER_PROFILE_ORGANIZER_NOT_FOUND));
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
                .orElseThrow(() -> new ProfileNotFoundException(MessageKeys.Users.USER_PROFILE_PARTICIPANT_NOT_FOUND));
        return ParticipantProfileResponse.fromDomain(profile);
    }

    /**
     * Saves the personal information of a user.
     *
     * @param userId  the unique identifier of the user whose personal information is to be saved
     * @param request an object containing the user's personal data, which includes first name, last name,
     *                second last name, birthdate, gender, and phone number
     */
    public void saveUserPersonalInformation(UUID userId, SavePersonalDataRequest request) {
        var useCase = new SaveUserPersonalInfoUseCase(userRepository);
        useCase.execute(request.toCommand(userId));
    }

    /**
     * Saves the address information of a user.
     *
     * @param userId  the unique identifier of the user whose address information is to be saved
     * @param request the data transfer object containing the address details, such as street, city, state,
     *                postal code, and country
     */
    public void saveUserAddress(UUID userId, SaveUserAddressRequest request) {
        var useCase = new SaveUserAddressUseCase(userRepository);
        useCase.execute(request.toCommand(userId));
    }

    /**
     * Retrieves user information based on the provided user ID.
     *
     * @param userId the unique identifier of the user whose information is to be retrieved
     * @return a UserInformationDto object containing detailed information about the user,
     * including personal data and address details
     * @throws UserNotFoundException if no user is found with the given ID
     */
    public UserInformationDto getUserInformation(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND));
        return new UserInformationDto(
                user.getId(),
                user.getEmail(),
                user.getPersonalData().getFirstName(),
                user.getPersonalData().getLastName(),
                user.getPersonalData().getSecondLastName(),
                user.getPersonalData().getBirthDate(),
                user.getPersonalData().getGender(),
                user.getPersonalData().getPhoneNumber(),
                AddressDto.fromDomain(user.getAddress())
        );
    }
}
