package com.accesosport.image.domain.repository;

import com.accesosport.image.domain.model.EventImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventImageRepository {

    EventImage save(EventImage image);

    List<EventImage> findByEventId(UUID eventId);

    Optional<EventImage> findById(UUID id);

    void delete(EventImage image);

    long countByEventId(UUID eventId);
}
