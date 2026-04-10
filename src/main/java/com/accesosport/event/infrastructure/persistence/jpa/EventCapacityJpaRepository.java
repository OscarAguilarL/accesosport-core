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

    /**
     * Atomically reserves one slot if the event is open and capacity is available.
     * Returns 1 if a slot was reserved, 0 if not (event not open or no capacity).
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE EventCapacityJpaEntity c
            SET c.reserved = c.reserved + 1
            WHERE c.eventId = :eventId
              AND (c.maxCapacity IS NULL OR c.reserved < c.maxCapacity)
              AND EXISTS (
                  SELECT 1 FROM EventJpaEntity e
                  WHERE e.id = :eventId
                    AND e.status = 'REGISTRATION_OPEN'
              )
            """)
    int reserveIfAvailable(@Param("eventId") UUID eventId);

    /**
     * Releases one slot on registration cancellation.
     * The WHERE reserved > 0 guard prevents going below zero.
     */
    @Modifying
    @Transactional
    @Query("""
            UPDATE EventCapacityJpaEntity c
            SET c.reserved = c.reserved - 1
            WHERE c.eventId = :eventId
              AND c.reserved > 0
            """)
    void release(@Param("eventId") UUID eventId);

    List<EventCapacityJpaEntity> findByEventIdIn(Collection<UUID> eventIds);
}
