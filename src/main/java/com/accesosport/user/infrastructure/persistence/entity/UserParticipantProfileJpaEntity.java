package com.accesosport.user.infrastructure.persistence.entity;

import com.accesosport.shared.domain.valueobjects.BloodType;
import com.accesosport.shared.domain.valueobjects.ShirtSize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the JPA entity for a user participant profile in the system. This class
 * maps to the `participant_profiles` table in the database and contains fields
 * related to personal, contact, and medical information of a participant.
 * It uses JPA annotations to define the entity structure and behavior for
 * persisting participant profile data.
 * <p>
 * The class includes lifecycle hooks to automatically populate the `createdAt`
 * and `updatedAt` timestamps during entity creation and update events.
 */
@Entity
@Table(name = "participant_profiles")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserParticipantProfileJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShirtSize shirtSize;

    @Column(nullable = false, length = 120)
    private String emergencyContactName;

    @Column(nullable = false, length = 20)
    private String emergencyContactPhone;

    @Column(length = 500)
    private String medicalConditions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BloodType bloodType;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserJpaEntity user;

    @PrePersist
    protected void atCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void atUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
