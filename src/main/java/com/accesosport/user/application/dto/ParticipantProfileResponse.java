package com.accesosport.user.application.dto;

import com.accesosport.shared.domain.valueobjects.BloodType;
import com.accesosport.shared.domain.valueobjects.ShirtSize;
import com.accesosport.user.domain.model.UserParticipantProfile;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the response containing the profile details of a participant in a system.
 * This is a Data Transfer Object (DTO) meant to encapsulate the relevant participant profile information.
 *
 * @param id                    Unique identifier for the participant.
 * @param shirtSize             The shirt size of the participant, represented by the {@code ShirtSize} enum.
 * @param emergencyContactName  The name of the emergency contact person for the participant.
 * @param emergencyContactPhone The phone number of the emergency contact person.
 * @param medicalConditions     Any medical conditions the participant has listed.
 * @param bloodType             The blood type of the participant, represented by the {@code BloodType} enum.
 * @param createdAt             The date and time when the participant profile was created.
 * @param updatedAt             The date and time when the participant profile was last updated.
 */
public record ParticipantProfileResponse(
        UUID id,
        ShirtSize shirtSize,
        String emergencyContactName,
        String emergencyContactPhone,
        String medicalConditions,
        BloodType bloodType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Converts a {@link UserParticipantProfile} domain object into a {@link ParticipantProfileResponse} DTO.
     *
     * @param participantProfile The domain object containing participant profile information.
     * @return A {@link ParticipantProfileResponse} that encapsulates the data from the provided domain object.
     */
    public static ParticipantProfileResponse fromDomain(UserParticipantProfile participantProfile) {
        return new ParticipantProfileResponse(
                participantProfile.getId(),
                participantProfile.getShirtSize(),
                participantProfile.getEmergencyContactName(),
                participantProfile.getEmergencyContactPhone(),
                participantProfile.getMedicalConditions(),
                participantProfile.getBloodType(),
                participantProfile.getCreatedAt(),
                participantProfile.getUpdatedAt()
        );
    }
}
