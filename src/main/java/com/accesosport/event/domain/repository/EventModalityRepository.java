package com.accesosport.event.domain.repository;

import com.accesosport.event.domain.model.EventModality;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventModalityRepository {
    EventModality save(EventModality modality);
    Optional<EventModality> findById(UUID id);
    List<EventModality> findByEventId(UUID eventId);
    List<EventModality> findByEventIdIn(Collection<UUID> eventIds);
    void deleteById(UUID id);
    int reserveIfAvailable(UUID modalityId);
    void release(UUID modalityId);
}
