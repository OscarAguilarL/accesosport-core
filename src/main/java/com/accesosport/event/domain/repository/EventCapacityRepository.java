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
}
