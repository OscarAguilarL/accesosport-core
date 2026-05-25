package com.accesosport.user.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents an embeddable entity for storing personal data associated with a user.
 * This class is designed to be embedded into other entities, encapsulating
 * personal information such as name, birthdate, and contact details.
 * It provides a structured way to hold this data consistently throughout the system.
 * The entity is annotated with JPA-related annotations and includes features for
 * automatic object building, immutability, and equality checking, using Lombok.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalDataEmbeddable {

    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String secondLastName;
    @Column
    private LocalDate birthDate;
    @Column
    private String gender;
    @Column
    private String phoneNumber;
}
