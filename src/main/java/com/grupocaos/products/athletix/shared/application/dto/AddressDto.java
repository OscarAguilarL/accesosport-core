package com.grupocaos.products.athletix.shared.application.dto;

import com.grupocaos.products.athletix.shared.domain.valueobjects.Address;
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
        @NotBlank(message = "La calle es obligatoria")
        @Size(max = 255, message = "La calle no puede exceder 255 caracteres")
        String street,

        @NotNull
        String externalNumber,

        String internalNumber,

        @NotNull
        String neighborhood,

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100, message = "La ciudad no puede exceder 100 caracteres")
        String city,

        @NotBlank(message = "El estado es obligatorio")
        @Size(min = 2, max = 50, message = "El estado debe tener entre 2 y 50 caracteres")
        String state,

        @NotNull
        String country,

        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(regexp = "^\\d{5}(-\\d{4})?$", message = "El código postal debe tener el formato: 12345 o 12345-6789")
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
