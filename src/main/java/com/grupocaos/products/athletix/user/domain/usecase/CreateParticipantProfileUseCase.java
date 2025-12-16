package com.grupocaos.products.athletix.user.domain.usecase;

import com.grupocaos.products.athletix.shared.domain.i18n.MessageKeys;
import com.grupocaos.products.athletix.shared.domain.usecase.UseCase;
import com.grupocaos.products.athletix.shared.domain.valueobjects.Address;
import com.grupocaos.products.athletix.shared.domain.valueobjects.BloodType;
import com.grupocaos.products.athletix.shared.domain.valueobjects.ShirtSize;
import com.grupocaos.products.athletix.user.domain.exception.UserNotFoundException;
import com.grupocaos.products.athletix.user.domain.model.Role;
import com.grupocaos.products.athletix.user.domain.model.RoleEnumeration;
import com.grupocaos.products.athletix.user.domain.model.User;
import com.grupocaos.products.athletix.user.domain.model.UserParticipantProfile;
import com.grupocaos.products.athletix.user.domain.repository.ParticipantProfileRepository;
import com.grupocaos.products.athletix.user.domain.repository.RoleRepository;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Use case to create the participant profile
 * At the moment of the creation the user is assigned with the role Participant
 */
@RequiredArgsConstructor
public class CreateParticipantProfileUseCase extends UseCase<CreateParticipantProfileUseCase.Command, CreateParticipantProfileUseCase.Result> {

    private final ParticipantProfileRepository participantProfileRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    protected Result internalExecute(Command command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND));

        if (user.hasRole(RoleEnumeration.ROLE_PARTICIPANT)) {
            throw new IllegalStateException(MessageKeys.Users.USER_PROFILE_PARTICIPANT_ALREADY_HAS_ROLE);
        }

        if (participantProfileRepository.findByUserId(command.userId()).isPresent()) {
            throw new IllegalStateException(MessageKeys.Users.USER_PROFILE_PARTICIPANT_ALREADY_HAS_PROFILE);
        }

        UserParticipantProfile profile = UserParticipantProfile.create(
                command.firstName(),
                command.lastName(),
                command.secondLastName(),
                command.birthDate(),
                command.gender(),
                command.phoneNumber(),
                command.address(),
                command.shirtSize(),
                command.emergencyContactName(),
                command.emergencyContactPhone(),
                command.medicalConditions(),
                command.bloodType(),
                user
        );

        var userRole = roleRepository.findByRole(RoleEnumeration.ROLE_PARTICIPANT)
                .orElseThrow(() -> new IllegalStateException("ROLE_PARTICIPANT not found"));
        user.addRole(userRole);
        user.addRole(Role.of(RoleEnumeration.ROLE_PARTICIPANT));
        userRepository.save(user);

        return new Result(participantProfileRepository.save(profile));
    }

    /**
     * The command to create the participant profile
     *
     * @param firstName             participant first name
     * @param lastName              participant last name
     * @param secondLastName        participant second last name
     * @param birthDate             the date of birth of the participan
     * @param gender                participant gender
     * @param phoneNumber           participant phone number
     * @param address               participant address
     * @param shirtSize             participant shirt size
     * @param emergencyContactName  participant emergency contact name
     * @param emergencyContactPhone participant emergency contact phone
     * @param medicalConditions     participant medical conditions
     * @param bloodType             participant blood type
     * @param userId                participant user id
     */
    public record Command(
            String firstName,
            String lastName,
            String secondLastName,
            LocalDate birthDate,
            String gender,
            String phoneNumber,
            Address address,
            ShirtSize shirtSize,
            String emergencyContactName,
            String emergencyContactPhone,
            String medicalConditions,
            BloodType bloodType,
            UUID userId
    ) {
    }

    /**
     * The result of the use case
     *
     * @param profile The ParticipantProfileResult object
     */
    public record Result(UserParticipantProfile profile) {
    }
}
