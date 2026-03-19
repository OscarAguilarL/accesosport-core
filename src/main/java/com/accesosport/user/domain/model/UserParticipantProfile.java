package com.accesosport.user.domain.model;

import com.accesosport.shared.domain.valueobjects.BloodType;
import com.accesosport.shared.domain.valueobjects.ShirtSize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
     * @param shirtSize             shirt size
     * @param emergencyContactName  emergency contact name
     * @param emergencyContactPhone emergency contact phone
     * @param medicalConditions     medical conditions of the participan
     * @param bloodType             blood type of the participant
     * @param user                  user assigned to the role
     * @return an object of type UserParticipantProfile
     */
    public static UserParticipantProfile create(
            ShirtSize shirtSize,
            String emergencyContactName,
            String emergencyContactPhone,
            String medicalConditions,
            BloodType bloodType,
            User user
    ) {
        return UserParticipantProfile.builder()
                .id(UUID.randomUUID())
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
