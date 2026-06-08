package com.accesosport.event.infrastructure.persistence.jpa;

import com.accesosport.event.infrastructure.persistence.entity.EventCapacityJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface EventCapacityJpaRepository extends JpaRepository<EventCapacityJpaEntity, UUID> {

    List<EventCapacityJpaEntity> findByEventIdIn(Collection<UUID> eventIds);

    @Modifying
    @Transactional
    @Query("""
            UPDATE EventCapacityJpaEntity c
            SET c.reserved = c.reserved + 1
            WHERE c.eventId = :eventId
              AND c.reserved < c.maxCapacity
              AND EXISTS (
                  SELECT 1 FROM EventJpaEntity e
                  WHERE e.id = :eventId
                    AND e.status = 'REGISTRATION_OPEN'
              )
            """)
    int reserveIfAvailable(@Param("eventId") UUID eventId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE EventCapacityJpaEntity c
            SET c.reserved = GREATEST(0, c.reserved - 1)
            WHERE c.eventId = :eventId
            """)
    void release(@Param("eventId") UUID eventId);
}
