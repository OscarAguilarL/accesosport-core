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
        EventCapacityJpaEntity entity = toEntity(capacity);
        return toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<EventCapacity> findByEventId(UUID eventId) {
        return jpaRepository.findById(eventId).map(this::toDomain);
    }

    @Override
    public List<EventCapacity> findAllByEventIdIn(Collection<UUID> eventIds) {
        return jpaRepository.findByEventIdIn(eventIds).stream()
                .map(this::toDomain)
                .toList();
    }

    private EventCapacity toDomain(EventCapacityJpaEntity entity) {
        return EventCapacity.reconstitute(entity.getEventId(), entity.getReserved(), entity.getMaxCapacity());
    }

    private EventCapacityJpaEntity toEntity(EventCapacity capacity) {
        return new EventCapacityJpaEntity(capacity.getEventId(), capacity.getReserved(), capacity.getMaxCapacity());
    }
}
