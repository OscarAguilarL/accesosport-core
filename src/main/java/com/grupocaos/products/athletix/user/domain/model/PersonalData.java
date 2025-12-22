package com.grupocaos.products.athletix.user.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents the personal data of an individual.
 * This class encapsulates basic information such as name, date of birth, gender, and contact details.
 * It is typically used to store and manage user or person-specific data within the application.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalData {

    private String firstName;
    private String lastName;
    private String secondLastName;
    private LocalDate birthDate;
    private String gender;
    private String phoneNumber;
}
