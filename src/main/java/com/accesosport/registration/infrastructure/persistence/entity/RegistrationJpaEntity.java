package com.accesosport.registration.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a JPA entity for storing information about a race event registration.
 */
@Entity
@Table(
        name = "registrations",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"event_id", "participant_id"}),
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

    @Column(name = "participant_id", nullable = false)
    private UUID participantId;

    @Column(name = "modality_id")
    private UUID modalityId;

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
}
