package com.grupocaos.products.athletix.event.domain.repository;

import com.grupocaos.products.athletix.event.domain.model.Event;
import com.grupocaos.products.athletix.event.domain.model.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {

    Optional<Event> findById(UUID id);

    List<Event> findAll();

    List<Event> findByStatus(EventStatus status);

    List<Event> findUpcomingEvents(LocalDateTime from, LocalDateTime to);

    List<Event> findByOrganizerId(UUID organizerId);

    List<Event> findEventosAvailableForRegistration();

    Event save(Event event);

    void delete(Event event);

    boolean existsById(UUID id);

    long countByStatus(EventStatus status);
}
