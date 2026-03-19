package com.accesosport.event.infrastructure.persistence.entity;

import com.accesosport.event.domain.model.DistanceUnit;
import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.domain.model.RaceType;
import com.accesosport.user.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a JPA entity for storing information about an event.
 */
@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Embedded
    private LocationEmbeddable location;

    @Enumerated(EnumType.STRING)
    @Column(name = "race_type", nullable = false, length = 30)
    private RaceType raceType;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal distance;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_unit", nullable = false, length = 15)
    private DistanceUnit distanceUnit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "registration_start", nullable = false)
    private LocalDateTime registrationStart;

    @Column(name = "registration_end", nullable = false)
    private LocalDateTime registrationEnd;

    @Column(name = "max_participants")
    private Integer maxParticipants;

    @Column(name = "registered_participants", nullable = false)
    private Integer registeredParticipants;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EventStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserJpaEntity createdBy;

    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @Column(name = "updated_on", nullable = false)
    private LocalDateTime updatedOn;

    @PrePersist
    protected void onCreate() {
        if (this.createdOn == null) {
            this.createdOn = LocalDateTime.now();
        }

        if (this.updatedOn == null) {
            this.updatedOn = LocalDateTime.now();
        }

        if (this.registeredParticipants == null) {
            registeredParticipants = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedOn = LocalDateTime.now();
    }
}
