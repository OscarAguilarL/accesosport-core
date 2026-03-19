package com.accesosport.event.infrastructure.persistence.adapter;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.event.infrastructure.persistence.entity.EventJpaEntity;
import com.accesosport.event.infrastructure.persistence.jpa.EventJpaRepository;
import com.accesosport.event.infrastructure.persistence.mapper.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter class for bridging the domain layer's {@link EventRepository} interface with the data layer's
 * {@link EventJpaRepository}. This class provides concrete implementations for retrieving, saving,
 * and deleting Event objects by mapping them between domain and persistence representations.
 * <p>
 * This adapter leverages the {@link EventMapper} to convert between domain objects and JPA entities.
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class EventRepositoryAdapter implements EventRepository {

    private final EventJpaRepository jpaRepository;

    @Override
    public Optional<Event> findById(UUID eventId) {
        return jpaRepository.findById(eventId)
                .map(EventMapper::toDomain);
    }

    @Override
    public List<Event> findAll() {
        return jpaRepository.findAll().stream()
                .map(EventMapper::toDomain)
                .toList();
    }

    @Override
    public List<Event> findByStatus(EventStatus status) {
        return jpaRepository.findByStatus(status).stream()
                .map(EventMapper::toDomain)
                .toList();
    }

    @Override
    public List<Event> findUpcomingEvents(LocalDateTime from, LocalDateTime to) {
        return jpaRepository.findUpcomingEvents(from, to).stream()
                .map(EventMapper::toDomain)
                .toList();
    }

    @Override
    public List<Event> findByOrganizerId(UUID organizerId) {
        return jpaRepository.findByOrganizerId(organizerId).stream()
                .map(EventMapper::toDomain)
                .toList();
    }

    @Override
    public List<Event> findEventsAvailableForRegistration() {
        return jpaRepository.findEventsAvailableForRegistration().stream()
                .map(EventMapper::toDomain)
                .toList();
    }

    @Override
    public Event save(Event event) {
        EventJpaEntity entity = EventMapper.toEntity(event);
        EventJpaEntity savedEntity = jpaRepository.save(entity);
        return EventMapper.toDomain(savedEntity);
    }

    @Override
    public void delete(Event event) {
        jpaRepository.delete(EventMapper.toEntity(event));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public long countByStatus(EventStatus status) {
        return jpaRepository.countByStatus(status);
    }
}
