package com.grupocaos.products.athletix.shared.domain.valueobjects;


import jakarta.annotation.Nonnull;

/**
 * @param street
 * @param city
 * @param state
 * @param zipCode
 */
public record Address(
        String street,
        String city,
        String state,
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
