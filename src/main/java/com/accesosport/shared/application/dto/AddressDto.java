package com.accesosport.shared.application.dto;

import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.valueobjects.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A Data Transfer Object (DTO) representation of an address, containing fields for street,
 * city, state, and zip code. Provides validation constraints and conversion methods
 * for integration with the domain model.
 * <p>
 * The record provides two methods for seamless mapping between the DTO and the domain Address object:
 * <p>
 * 1. {@code toDomain()}: Converts an instance of AddressDto to the corresponding domain Address object.
 * 2. {@code fromDomain(Address)}: Converts a domain Address object to an AddressDto instance.
 *
 * @param city    Represents the street information; validated for presence and maximum length.
 * @param state   Represents the city; validated for presence and maximum length.
 * @param street  Represents the state; validated for presence and constrained within a length range.
 * @param zipCode Represents the postal code; validated for presence and formatting compliance.
 */
public record AddressDto(
        @NotBlank(message = MessageKeys.Users.USER_ADDRESS_VALIDATION_STREET_REQUIRED)
        @Size(max = 255, message = MessageKeys.Users.USER_ADDRESS_VALIDATION_STREET_LENGTH)
        String street,

        @NotNull
        String externalNumber,

        String internalNumber,

        @NotNull
        String neighborhood,

        @NotBlank(message = MessageKeys.Users.USER_ADDRESS_VALIDATION_CITY_REQUIRED)
        @Size(max = 100, message = MessageKeys.Users.USER_ADDRESS_VALIDATION_CITY_LENGTH)
        String city,

        @NotBlank(message = MessageKeys.Users.USER_ADDRESS_VALIDATION_STATE_REQUIRED)
        @Size(min = 2, max = 50, message = MessageKeys.Users.USER_ADDRESS_VALIDATION_STATE_LENGTH)
        String state,

        @NotNull
        String country,

        @NotBlank(message = MessageKeys.Users.USER_ADDRESS_VALIDATION_ZIPCODE_REQUIRED)
        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = MessageKeys.Users.USER_ADDRESS_VALIDATION_ZIPCODE_FORMAT)
        String zipCode
) {
    /**
     * Converts this AddressDto instance to its corresponding domain Address object.
     *
     * @return a new Address instance with values mapped from this AddressDto.
     */
    public Address toDomain() {
        return new Address(street, externalNumber, internalNumber, neighborhood, city, state, country, zipCode);
    }

    /**
     * Converts a domain Address object to an AddressDto instance.
     *
     * @param address the Address domain object to be converted
     * @return a new AddressDto instance with values mapped from the given Address
     */
    public static AddressDto fromDomain(Address address) {
        return new AddressDto(
                address.street(),
                address.externalNumber(),
                address.internalNumber(),
                address.neighborhood(),
                address.city(),
                address.state(),
                address.country(),
                address.zipCode()
        );
    }
}
