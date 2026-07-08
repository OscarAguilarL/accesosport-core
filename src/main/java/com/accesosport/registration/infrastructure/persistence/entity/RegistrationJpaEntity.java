package com.accesosport.registration.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "registrations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"ticket_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(name = "participant_id")
    private UUID participantId;

    @Column(name = "modality_id")
    private UUID modalityId;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "ticket_code", nullable = false, length = 9)
    private String ticketCode;

    @Column(name = "bib_number")
    private Integer bibNumber;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "kit_picked_up", nullable = false, columnDefinition = "boolean default false")
    private boolean kitPickedUp;

    @Column(name = "kit_picked_up_at")
    private LocalDateTime kitPickedUpAt;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "waiver_accepted_at")
    private LocalDateTime waiverAcceptedAt;

    @Column(name = "waiver_text", columnDefinition = "TEXT")
    private String waiverText;

    @Column(name = "wants_shirt", nullable = false, columnDefinition = "boolean default true")
    private boolean wantsShirt = true;

    @Column(name = "participant_email")
    private String participantEmail;

    @Column(name = "participant_first_name")
    private String participantFirstName;

    @Column(name = "participant_last_name")
    private String participantLastName;

    @Column(name = "participant_phone")
    private String participantPhone;

    @Column(name = "shirt_size")
    private String shirtSize;

    @Column(name = "blood_type")
    private String bloodType;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "medical_conditions", columnDefinition = "TEXT")
    private String medicalConditions;

    @Column(name = "payment_access_token_hash", length = 64)
    private String paymentAccessTokenHash;

    @Column(name = "payment_access_token_expires_at")
    private java.time.LocalDateTime paymentAccessTokenExpiresAt;
}
