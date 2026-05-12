package com.accesosport.event.domain.repository;

import com.accesosport.event.domain.model.EventCategory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventCategoryRepository {
    EventCategory save(EventCategory category);
    Optional<EventCategory> findById(UUID id);
    List<EventCategory> findByEventId(UUID eventId);
    void deleteById(UUID id);
}
