package com.accesosport.event.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "event_capacity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventCapacityJpaEntity {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Column(name = "reserved", nullable = false)
    private int reserved;
}
