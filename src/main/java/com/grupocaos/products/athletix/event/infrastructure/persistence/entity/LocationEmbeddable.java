package com.grupocaos.products.athletix.event.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents an embeddable value object for storing location-related data.
 * <p>
 * This class is designed to be embedded within an entity to persist
 * information about a specific place, including its name, city, country,
 * and geographical coordinates (latitude and longitude).
 * </p>
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationEmbeddable {

    @Column(nullable = false, length = 500)
    String place;

    @Column(length = 100)
    String city;

    @Column(length = 100)
    String country;

    @Column(precision = 10, scale = 7)
    BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    BigDecimal longitude;
}
