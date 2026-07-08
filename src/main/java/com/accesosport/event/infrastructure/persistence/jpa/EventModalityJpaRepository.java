package com.accesosport.event.infrastructure.persistence.jpa;

import com.accesosport.event.infrastructure.persistence.entity.EventModalityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EventModalityJpaRepository extends JpaRepository<EventModalityJpaEntity, UUID> {

    List<EventModalityJpaEntity> findByEventId(UUID eventId);

    List<EventModalityJpaEntity> findByEventIdIn(Collection<UUID> eventIds);

    @Modifying
    @Transactional
    @Query("""
            UPDATE EventModalityJpaEntity m
            SET m.registeredCount = m.registeredCount + 1
            WHERE m.id = :modalityId
            """)
    void incrementRegisteredCount(@Param("modalityId") UUID modalityId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE EventModalityJpaEntity m
            SET m.registeredCount = GREATEST(0, m.registeredCount - 1)
            WHERE m.id = :modalityId
            """)
    void release(@Param("modalityId") UUID modalityId);
}
