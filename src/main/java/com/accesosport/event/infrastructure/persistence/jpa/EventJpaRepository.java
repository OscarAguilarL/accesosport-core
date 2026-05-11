package com.accesosport.event.infrastructure.persistence.jpa;

import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.infrastructure.persistence.entity.EventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for performing CRUD and custom operations on EventJpaEntity.
 * Extends JpaRepository to provide standard data access methods for EventJpaEntity.
 */
public interface EventJpaRepository extends JpaRepository<EventJpaEntity, UUID> {

    /**
     * Retrieves a list of events filtered by their status.
     *
     * @param status the status of the events to filter by
     * @return a list of {@code EventJpaEntity} objects that match the specified status
     */
    List<EventJpaEntity> findByStatus(EventStatus status);

    /**
     * Retrieves a list of upcoming events that occur within the specified date range.
     *
     * @param from the start date and time of the range to filter events
     * @param to   the end date and time of the range to filter events
     * @return a list of {@code EventJpaEntity} objects occurring within the specified date range,
     * ordered by event date
     */
    @Query("SELECT e FROM EventJpaEntity e WHERE e.eventDate BETWEEN :from AND :to ORDER BY e.eventDate")
    List<EventJpaEntity> findUpcomingEvents(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    /**
     * Retrieves a list of events organized by a specific organizer, identified by their unique ID.
     * The list is ordered by event date in descending order.
     *
     * @param organizerId the unique identifier of the organizer whose events are to be retrieved
     * @return a list of {@code EventJpaEntity} objects associated with the given organizer ID,
     * ordered by event date in descending order
     */
    @Query("SELECT e FROM EventJpaEntity e WHERE e.createdBy.id = :organizerId ORDER BY e.eventDate DESC")
    List<EventJpaEntity> findByOrganizerId(@Param("organizerId") UUID organizerId);

    /**
     * Retrieves a list of events that are currently open for registration.
     * The events must have a status of 'REGISTRATION_OPEN', occur after the current timestamp,
     * and either have no participant limit or have available participant slots.
     * The results are ordered by event date in ascending order.
     *
     * @return a list of {@code EventJpaEntity} objects that meet the criteria for registration availability
     */
    @Query("""
            SELECT e FROM EventJpaEntity e
            WHERE e.status = 'REGISTRATION_OPEN'
            AND e.eventDate > CURRENT_TIMESTAMP
            AND EXISTS (
                SELECT 1 FROM EventModalityJpaEntity m
                WHERE m.eventId = e.id
                  AND m.registeredCount < m.capacity
            )
            ORDER BY e.eventDate
            """)
    List<EventJpaEntity> findEventsAvailableForRegistration();

    /**
     * Counts the number of events with the specified status.
     *
     * @param status the status of the events to count
     * @return the number of events matching the specified status
     */
    long countByStatus(EventStatus status);

    @Query("SELECT e FROM EventJpaEntity e WHERE e.status = 'PUBLISHED' AND e.registrationStart <= :now")
    List<EventJpaEntity> findEventsReadyToOpenRegistration(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM EventJpaEntity e WHERE e.status = 'REGISTRATION_OPEN' AND e.registrationEnd <= :now")
    List<EventJpaEntity> findEventsReadyToCloseRegistration(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM EventJpaEntity e WHERE e.status = 'REGISTRATION_CLOSED' AND e.eventDate <= :now")
    List<EventJpaEntity> findEventsReadyToBegin(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM EventJpaEntity e WHERE e.status = 'IN_PROGRESS' AND e.eventDate <= :threshold")
    List<EventJpaEntity> findEventsReadyToComplete(@Param("threshold") LocalDateTime threshold);

    @Query("""
            SELECT e FROM EventJpaEntity e
            WHERE e.eventDate BETWEEN :from AND :to
              AND e.reminderSentAt IS NULL
              AND e.status IN ('REGISTRATION_CLOSED', 'IN_PROGRESS')
            """)
    List<EventJpaEntity> findEventsNeedingReminder(@Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);
}
