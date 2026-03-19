package com.accesosport.shared.infrastructure.common.persistence.jpa;

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

    @Column()
    private String street;

    @Column()
    private String externalNumber;

    @Column
    private String internalNumber;

    @Column()
    private String neighborhood;

    @Column(length = 100)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(length = 50)
    private String country;

    @Column(length = 10)
    private String zipCode;
}
