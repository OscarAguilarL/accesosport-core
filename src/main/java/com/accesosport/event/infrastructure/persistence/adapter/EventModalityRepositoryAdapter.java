package com.accesosport.event.infrastructure.persistence.adapter;

import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.infrastructure.persistence.entity.EventModalityJpaEntity;
import com.accesosport.event.infrastructure.persistence.jpa.EventModalityJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EventModalityRepositoryAdapter implements EventModalityRepository {

    private final EventModalityJpaRepository jpaRepository;

    @Override
    public EventModality save(EventModality modality) {
        return toDomain(jpaRepository.save(toEntity(modality)));
    }

    @Override
    public Optional<EventModality> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<EventModality> findByEventId(UUID eventId) {
        return jpaRepository.findByEventId(eventId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<EventModality> findByEventIdIn(Collection<UUID> eventIds) {
        return jpaRepository.findByEventIdIn(eventIds).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public int reserveIfAvailable(UUID modalityId) {
        return jpaRepository.reserveIfAvailable(modalityId);
    }

    @Override
    public void release(UUID modalityId) {
        jpaRepository.release(modalityId);
    }

    private EventModality toDomain(EventModalityJpaEntity e) {
        return EventModality.reconstitute(e.getId(), e.getEventId(), e.getName(), e.getDistance(), e.getDistanceUnit(), e.getPrice(), e.getCapacity(), e.getRegisteredCount());
    }

    private EventModalityJpaEntity toEntity(EventModality m) {
        return new EventModalityJpaEntity(m.getId(), m.getEventId(), m.getName(), m.getDistance(), m.getDistanceUnit(), m.getPrice(), m.getCapacity(), m.getRegisteredCount());
    }
}
