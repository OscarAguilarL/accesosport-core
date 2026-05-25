package com.accesosport.user.application.dto;

import com.accesosport.user.domain.usecase.SaveUserAddressUseCase;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Represents a request to save a user's address details.
 * This is a Data Transfer Object (DTO) used to encapsulate the necessary
 * information for recording or updating an address in the system.
 *
 * @param street         The name of the street for the user's address. It is a mandatory field.
 * @param externalNumber The external number of the user's address. It is a mandatory field.
 * @param internalNumber The internal number or unit of the user's address. It is an optional field.
 * @param neighborhood   The neighborhood where the user's address is located. It is a mandatory field.
 * @param city           The city of the user's address. It is a mandatory field.
 * @param state          The state or region of the user's address. It is a mandatory field.
 * @param country        The country of the user's address. It is a mandatory field.
 * @param zipCode        The postal or zip code of the user's address. It is a mandatory field.
 */
public record SaveUserAddressRequest(
        @NotBlank
        String street,

        @NotBlank
        String externalNumber,

        String internalNumber,

        @NotBlank
        String neighborhood,

        @NotBlank
        String city,

        @NotBlank
        String state,

        @NotBlank
        String country,

        @NotBlank
        String zipCode
) {

    /**
     * Converts the current SaveUserAddressRequest instance into a SaveUserAddressUseCase.Command object.
     * This method transforms the input user address details and the provided user identifier into
     * a command object that can be used to execute the save or update address use case.
     *
     * @param userId The unique identifier of the user whose address is being saved or updated.
     * @return A new instance of SaveUserAddressUseCase.Command containing the user's identifier and all address details.
     */
    public SaveUserAddressUseCase.Command toCommand(
            UUID userId
    ) {
        return new SaveUserAddressUseCase.Command(
                userId,
                street,
                externalNumber,
                internalNumber,
                neighborhood,
                city,
                state,
                country,
                zipCode
        );
    }
}
