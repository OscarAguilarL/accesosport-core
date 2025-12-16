package com.grupocaos.products.athletix.user.application.dto;

import com.grupocaos.products.athletix.shared.application.dto.AddressDto;
import com.grupocaos.products.athletix.shared.domain.valueobjects.BloodType;
import com.grupocaos.products.athletix.shared.domain.valueobjects.ShirtSize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Represents a request to create a participant profile, capturing personal, contact, and medical details.
 *
 * @param firstName             The first name of the participant. Must not be blank.
 * @param lastName              The last name of the participant. Must not be blank.
 * @param secondLastName        The second last name of the participant. Optional.
 * @param birthDate             The date of birth of the participant. Must not be null.
 * @param gender                The gender of the participant. Must not be blank.
 * @param phoneNumber           The participant's contact phone number. Must not be blank and must have a length between 7 and 15 characters.
 * @param address               The address details of the participant. Must not be null.
 * @param shirtSize             The shirt size of the participant. Must not be null and represented by a valid {@link ShirtSize}.
 * @param emergencyContactName  The name of the emergency contact. Must not be blank.
 * @param emergencyContactPhone The phone number of the emergency contact. Must not be blank.
 * @param medicalConditions     Medical conditions of the participant, if any. Optional, with a maximum length of 500 characters.
 * @param bloodType             The blood type of the participant. Must not be null and represented by a valid {@link BloodType}.
 */
public record CreateParticipantProfileRequest(

        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        String secondLastName,

        @NotNull
        LocalDate birthDate,

        @NotBlank
        String gender,

        @NotBlank
        @Size(min = 7, max = 15)
        String phoneNumber,

        @NotNull
        AddressDto address,

        @NotNull
        ShirtSize shirtSize,

        @NotBlank
        String emergencyContactName,

        @NotBlank
        String emergencyContactPhone,

        @Size(max = 500)
        String medicalConditions,

        @NotNull
        BloodType bloodType
) {
}
