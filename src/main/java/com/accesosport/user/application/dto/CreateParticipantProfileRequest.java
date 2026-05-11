package com.accesosport.user.application.dto;

import com.accesosport.shared.domain.valueobjects.BloodType;
import com.accesosport.shared.domain.valueobjects.Gender;
import com.accesosport.shared.domain.valueobjects.ShirtSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents a request to create a participant profile, capturing personal, contact, and medical details.
 *
 * @param shirtSize             The shirt size of the participant. Must not be null and represented by a valid {@link ShirtSize}.
 * @param emergencyContactName  The name of the emergency contact. Must not be blank.
 * @param emergencyContactPhone The phone number of the emergency contact. Must not be blank.
 * @param medicalConditions     Medical conditions of the participant, if any. Optional, with a maximum length of 500 characters.
 * @param bloodType             The blood type of the participant. Must not be null and represented by a valid {@link BloodType}.
 */
public record CreateParticipantProfileRequest(

        @NotNull
        ShirtSize shirtSize,

        @NotBlank
        String emergencyContactName,

        @NotBlank
        String emergencyContactPhone,

        @Size(max = 500)
        String medicalConditions,

        @NotNull
        BloodType bloodType,

        @NotBlank
        String phone,

        @NotNull
        Gender gender
) {
}
