package com.grupocaos.products.athletix.shared.domain.valueobjects;


public record Address(
        String street,
        String city,
        String state,
        String zipCode
) {}
