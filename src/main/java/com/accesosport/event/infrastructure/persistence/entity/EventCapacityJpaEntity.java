package com.accesosport.event.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Persistence entity for the event_capacity table.
 * event_id is the PK, enforcing a 1:1 relationship with the events table.
 */
@Entity
@Table(name = "event_capacity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCapacityJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "reserved", nullable = false)
    private int reserved;

    @Column(name = "max_capacity")
    private Integer maxCapacity; // null = unlimited
}
