package com.accesosport.event.infrastructure.persistence.adapter;

import com.accesosport.event.domain.model.EventCapacity;
import com.accesosport.event.domain.repository.EventCapacityRepository;
import com.accesosport.event.infrastructure.persistence.entity.EventCapacityJpaEntity;
import com.accesosport.event.infrastructure.persistence.jpa.EventCapacityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EventCapacityRepositoryAdapter implements EventCapacityRepository {

    private final EventCapacityJpaRepository jpaRepository;

    @Override
    public EventCapacity save(EventCapacity capacity) {
        return toDomain(jpaRepository.save(toEntity(capacity)));
    }

    @Override
    public Optional<EventCapacity> findByEventId(UUID eventId) {
        return jpaRepository.findById(eventId).map(this::toDomain);
    }

    @Override
    public List<EventCapacity> findByEventIdIn(Collection<UUID> eventIds) {
        return jpaRepository.findByEventIdIn(eventIds).stream().map(this::toDomain).toList();
    }

    @Override
    public int reserveIfAvailable(UUID eventId) {
        return jpaRepository.reserveIfAvailable(eventId);
    }

    @Override
    public void release(UUID eventId) {
        jpaRepository.release(eventId);
    }

    private EventCapacity toDomain(EventCapacityJpaEntity e) {
        return EventCapacity.reconstitute(e.getEventId(), e.getReserved(), e.getMaxCapacity());
    }

    private EventCapacityJpaEntity toEntity(EventCapacity c) {
        return new EventCapacityJpaEntity(c.getEventId(), c.getMaxCapacity(), c.getReserved());
    }
}
