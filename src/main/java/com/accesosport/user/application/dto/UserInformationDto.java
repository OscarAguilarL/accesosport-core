package com.accesosport.user.application.dto;

import com.accesosport.shared.application.dto.AddressDto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Data Transfer Object (DTO) representing user information.
 * This class encapsulates the necessary fields to represent a user's
 * details, including personal information and address data.
 *
 * @param id             Unique identifier of the user.
 * @param email          Email address of the user.
 * @param firstName      The first name of the user.
 * @param lastName       The last name of the user, often referred to as the paternal last name.
 * @param secondLastName The second last name of the user, often referred to as the maternal last name.
 * @param birthDate      The date of birth of the user.
 * @param gender         The gender of the user.
 * @param phoneNumber    The contact phone number of the user.
 * @param address        The address details of the user, represented by an AddressDto object.
 */
public record UserInformationDto(
        UUID id,
        String email,
        String firstName,
        String lastName,
        String secondLastName,
        LocalDate birthDate,
        String gender,
        String phoneNumber,
        AddressDto address
) {
}
