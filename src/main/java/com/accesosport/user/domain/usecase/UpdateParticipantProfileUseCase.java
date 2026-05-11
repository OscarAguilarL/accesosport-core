package com.accesosport.user.domain.usecase;

import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.shared.domain.valueobjects.BloodType;
import com.accesosport.shared.domain.valueobjects.Gender;
import com.accesosport.shared.domain.valueobjects.ShirtSize;
import com.accesosport.user.domain.exception.UserNotFoundException;
import com.accesosport.user.domain.model.UserParticipantProfile;
import com.accesosport.user.domain.repository.ParticipantProfileRepository;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UpdateParticipantProfileUseCase extends UseCase<UpdateParticipantProfileUseCase.Command, UpdateParticipantProfileUseCase.Result> {

    private final ParticipantProfileRepository participantProfileRepository;
    private final UserRepository userRepository;

    @Override
    protected Result internalExecute(Command command) {
        userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(MessageKeys.AuthMessages.USER_NOT_FOUND));

        UserParticipantProfile profile = participantProfileRepository.findByUserId(command.userId())
                .orElseThrow(() -> new IllegalStateException(MessageKeys.Users.USER_PROFILE_PARTICIPANT_NOT_FOUND));

        profile.setShirtSize(command.shirtSize());
        profile.setEmergencyContactName(command.emergencyContactName());
        profile.setEmergencyContactPhone(command.emergencyContactPhone());
        profile.setMedicalConditions(command.medicalConditions());
        profile.setBloodType(command.bloodType());
        profile.setPhone(command.phone());
        profile.setGender(command.gender());

        return new Result(participantProfileRepository.save(profile));
    }

    public record Command(
            ShirtSize shirtSize,
            String emergencyContactName,
            String emergencyContactPhone,
            String medicalConditions,
            BloodType bloodType,
            String phone,
            Gender gender,
            UUID userId
    ) {
    }

    public record Result(UserParticipantProfile profile) {
    }
}
