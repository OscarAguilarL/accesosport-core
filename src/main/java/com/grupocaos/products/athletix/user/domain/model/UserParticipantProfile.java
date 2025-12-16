package com.grupocaos.products.athletix.user.domain.model;

import com.grupocaos.products.athletix.shared.domain.valueobjects.Address;
import com.grupocaos.products.athletix.shared.domain.valueobjects.BloodType;
import com.grupocaos.products.athletix.shared.domain.valueobjects.ShirtSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Model for the runner participant profile
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserParticipantProfile {

    private UUID id;

    // Personal data
    private String firstName;
    private String lastName;
    private String secondLastName;
    private LocalDate birthDate;
    private String gender;
    private String phoneNumber;

    private Address address;

    // RUNNER SPECIFIC DATA
    private ShirtSize shirtSize;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String medicalConditions;
    private BloodType bloodType;

    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates an object of UserParticipantProfile
     *
     * @param firstName             first name
     * @param lastName              last name
     * @param secondLastName        second last name
     * @param birthDate             date of birth
     * @param Gender                gender
     * @param phoneNumber           phone number
     * @param address               address
     * @param shirtSize             shirt size
     * @param emergencyContactName  emergency contact name
     * @param emergencyContactPhone emergency contact phone
     * @param medicalConditions     medical conditions of the participan
     * @param bloodType             blood type of the participant
     * @param user                  user assigned to the role
     * @return an object of type UserParticipantProfile
     */
    public static UserParticipantProfile create(
            String firstName,
            String lastName,
            String secondLastName,
            LocalDate birthDate,
            String Gender,
            String phoneNumber,
            Address address,
            ShirtSize shirtSize,
            String emergencyContactName,
            String emergencyContactPhone,
            String medicalConditions,
            BloodType bloodType,
            User user
    ) {
        return UserParticipantProfile.builder()
                .id(UUID.randomUUID())
                .firstName(firstName)
                .lastName(lastName)
                .secondLastName(secondLastName)
                .birthDate(birthDate)
                .gender(Gender)
                .phoneNumber(phoneNumber)
                .address(address)
                .shirtSize(shirtSize)
                .emergencyContactName(emergencyContactName)
                .emergencyContactPhone(emergencyContactPhone)
                .medicalConditions(medicalConditions)
                .bloodType(bloodType)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

    }
}
