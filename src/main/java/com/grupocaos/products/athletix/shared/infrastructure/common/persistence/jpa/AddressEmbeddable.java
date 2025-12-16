package com.grupocaos.products.athletix.shared.infrastructure.common.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an embeddable Address entity that can be used as a composite object
 * in database entities. This class is annotated with JPA's {@code @Embeddable}
 * to indicate that it can be embedded into other entity classes.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressEmbeddable {

    @Column(nullable = false)
    private String street;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 10)
    private String zipCode;

    // TODO: private String externalNumber;
    // TODO: private String internalNumber;
    // TODO: private String neighborhood;
    // TODO: private String country;
}
