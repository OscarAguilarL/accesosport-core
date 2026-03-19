package com.accesosport.shared.domain.valueobjects;


import jakarta.annotation.Nonnull;

/**
 * Represents a user's address, encapsulating various address components such as
 * street name, house numbers, neighborhood, city, state, country, and zip code.
 *
 * @param street         Primary street name or description.
 * @param externalNumber Street-facing numeric or alphanumeric identifier.
 * @param internalNumber Optional identifier for specific areas within a larger premise.
 * @param neighborhood   Subdivision or smaller community within a city.
 * @param city           City where the address is located.
 * @param state          State or province where the address is located.
 * @param country        Country where the address is located.
 * @param zipCode        Postal code associated with the address for mail or logistics purposes.
 */
public record Address(
        String street,
        String externalNumber,
        String internalNumber,
        String neighborhood,
        String city,
        String state,
        String country,
        String zipCode
) {
    @Override
    @Nonnull
    public String toString() {
        StringBuilder result = new StringBuilder(street);
        if (city != null && !city.isBlank()) {
            result.append(", ").append(city);
        }
        if (state != null && !state.isBlank()) {
            result.append(", ").append(state);
        }
        return result.toString();
    }
}
