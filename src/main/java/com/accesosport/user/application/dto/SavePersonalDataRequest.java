package com.accesosport.user.application.dto;

import com.accesosport.user.domain.usecase.SaveUserPersonalInfoUseCase;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a request to save personal data for an individual.
 * This is a Data Transfer Object (DTO) used to encapsulate the necessary
 * details for saving personal information.
 *
 * @param firstName      The first name of the individual.
 * @param lastName       The last name of the individual. Often referred to as the paternal last name.
 * @param secondLastName The second last name of the individual. Often referred to as the maternal last name.
 * @param birthDate      The birthdate of the individual, represented as a LocalDate.
 * @param gender         The gender of the individual.
 * @param phoneNumber    The phone number of the individual.
 */
public record SavePersonalDataRequest(
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
        @Size(min = 10, max = 15)
        String phoneNumber
) {

    /**
     * Converts the current data into a {@link SaveUserPersonalInfoUseCase.Command} object.
     * This method prepares the required command object for updating a user's personal information
     * within the designated use case.
     *
     * @param userId The unique identifier of the user for whom the command is being created.
     * @return An instance of {@link SaveUserPersonalInfoUseCase.Command} that contains the user's
     * personal data and is ready to be processed by the use case.
     */
    public SaveUserPersonalInfoUseCase.Command toCommand(UUID userId) {
        return new SaveUserPersonalInfoUseCase.Command(
                userId,
                firstName,
                lastName,
                secondLastName,
                birthDate,
                gender,
                phoneNumber
        );
    }
}
