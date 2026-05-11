package com.accesosport.event.infrastructure.persistence.entity;

import com.accesosport.event.domain.model.DistanceUnit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "event_modalities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventModalityJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal distance;

    @Enumerated(EnumType.STRING)
    @Column(name = "distance_unit", nullable = false, length = 15)
    private DistanceUnit distanceUnit;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "registered_count", nullable = false, columnDefinition = "int default 0")
    private int registeredCount;
}
