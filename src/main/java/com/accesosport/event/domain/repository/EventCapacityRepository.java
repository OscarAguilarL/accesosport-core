package com.accesosport.event.domain.repository;

import com.accesosport.event.domain.model.EventCapacity;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventCapacityRepository {

    EventCapacity save(EventCapacity capacity);

    Optional<EventCapacity> findByEventId(UUID eventId);

    List<EventCapacity> findAllByEventIdIn(Collection<UUID> eventIds);

    /**
     * Atomically reserves one slot if the event is open and capacity is available.
     * Returns 1 if reserved, 0 if not available or event not open.
     */
    int reserveIfAvailable(UUID eventId);

    /**
     * Releases one reserved slot. Guards against going below zero.
     */
    void release(UUID eventId);
}
