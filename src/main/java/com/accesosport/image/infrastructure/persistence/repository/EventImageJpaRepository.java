package com.accesosport.image.infrastructure.persistence.repository;

import com.accesosport.image.infrastructure.persistence.entity.EventImageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventImageJpaRepository extends JpaRepository<EventImageJpaEntity, UUID> {

    List<EventImageJpaEntity> findByEventIdOrderByDisplayOrderAsc(UUID eventId);

    long countByEventId(UUID eventId);
}
