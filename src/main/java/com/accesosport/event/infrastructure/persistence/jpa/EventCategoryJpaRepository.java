package com.accesosport.event.infrastructure.persistence.jpa;

import com.accesosport.event.infrastructure.persistence.entity.EventCategoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventCategoryJpaRepository extends JpaRepository<EventCategoryJpaEntity, UUID> {
    List<EventCategoryJpaEntity> findByEventId(UUID eventId);
}
