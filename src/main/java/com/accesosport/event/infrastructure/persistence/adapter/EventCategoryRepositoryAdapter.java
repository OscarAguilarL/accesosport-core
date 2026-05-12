package com.accesosport.event.infrastructure.persistence.adapter;

import com.accesosport.event.domain.model.EventCategory;
import com.accesosport.event.domain.repository.EventCategoryRepository;
import com.accesosport.event.infrastructure.persistence.entity.EventCategoryJpaEntity;
import com.accesosport.event.infrastructure.persistence.jpa.EventCategoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class EventCategoryRepositoryAdapter implements EventCategoryRepository {

    private final EventCategoryJpaRepository jpaRepository;

    @Override
    public EventCategory save(EventCategory category) {
        return toDomain(jpaRepository.save(toEntity(category)));
    }

    @Override
    public Optional<EventCategory> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<EventCategory> findByEventId(UUID eventId) {
        return jpaRepository.findByEventId(eventId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    private EventCategory toDomain(EventCategoryJpaEntity e) {
        return EventCategory.reconstitute(e.getId(), e.getEventId(), e.getModalityId(), e.getName(), e.getMinAge(), e.getMaxAge());
    }

    private EventCategoryJpaEntity toEntity(EventCategory c) {
        return new EventCategoryJpaEntity(c.getId(), c.getEventId(), c.getModalityId(), c.getName(), c.getMinAge(), c.getMaxAge());
    }
}
