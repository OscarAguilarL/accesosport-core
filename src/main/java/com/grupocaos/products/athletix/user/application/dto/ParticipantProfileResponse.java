package com.grupocaos.products.athletix.user.application.dto;

import com.grupocaos.products.athletix.shared.application.dto.AddressDto;
import com.grupocaos.products.athletix.shared.domain.valueobjects.BloodType;
import com.grupocaos.products.athletix.shared.domain.valueobjects.ShirtSize;
import com.grupocaos.products.athletix.user.domain.model.UserParticipantProfile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the response containing the profile details of a participant in a system.
 * This is a Data Transfer Object (DTO) meant to encapsulate the relevant participant profile information.
 *
 * @param id                    Unique identifier for the participant.
 * @param firstName             The first name of the participant.
 * @param lastName              The last name of the participant.
 * @param secondLastName        The second last name of the participant.
 * @param birthDate             The date of birth of the participant.
 * @param gender                The gender of the participant.
 * @param phoneNumber           The phone number of the participant.
 * @param address               The AddressDto object containing the address details of the participant.
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
        String firstName,
        String lastName,
        String secondLastName,
        LocalDate birthDate,
        String gender,
        String phoneNumber,
        AddressDto address,
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
                participantProfile.getFirstName(),
                participantProfile.getLastName(),
                participantProfile.getSecondLastName(),
                participantProfile.getBirthDate(),
                participantProfile.getGender(),
                participantProfile.getPhoneNumber(),
                AddressDto.fromDomain(participantProfile.getAddress()),
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
