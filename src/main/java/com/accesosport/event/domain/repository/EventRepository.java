package com.accesosport.event.domain.repository;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventRepository {

    /**
     * Finds an event by its identifier.
     *
     * @param eventId Event identifier to be searched
     * @return Event entity if found, otherwise empty
     */
    Optional<Event> findById(UUID eventId);

    /**
     * Finds all events.
     *
     * @return List of all events
     */
    List<Event> findAll();

    /**
     * Finds all events with the specified status.
     *
     * @param status Event status to be searched.
     * @return List of events with the specified status.
     */
    List<Event> findByStatus(EventStatus status);

    /**
     * Finds events that are scheduled to occur within the specified time range.
     *
     * @param from the starting point of the time range (inclusive)
     * @param to   the ending point of the time range (inclusive)
     * @return a list of events occurring within the specified time range
     */
    List<Event> findUpcomingEvents(LocalDateTime from, LocalDateTime to);

    /**
     * Finds all events associated with a specific organizer.
     *
     * @param organizerId the unique identifier of the organizer whose events should be retrieved
     * @return a list of events organized by the specified organizer, or an empty list if none are found
     */
    List<Event> findByOrganizerId(UUID organizerId);

    /**
     * Retrieves a list of events that are currently available for registration.
     * An event is considered available for registration if its status allows
     * registrations, the registration period is open, and the maximum number
     * of participants has not been reached (if a limit is set).
     *
     * @return a list of events that meet the criteria for open registration or an empty list if no events are available
     */
    List<Event> findEventsAvailableForRegistration();

    /**
     * Persists the given event entity into the data store.
     * If the event does not already exist, it will be created;
     * otherwise, the existing event will be updated with the new data.
     *
     * @param event The event entity to be persisted.
     * @return The saved event entity, including any updates made during the save process.
     */
    Event save(Event event);

    /**
     * Deletes the specified event from the data store.
     *
     * @param event The event entity to be deleted. Must not be null.
     */
    void delete(Event event);

    /**
     * Checks whether an entity with the specified unique identifier exists in the repository.
     *
     * @param id the unique identifier of the entity to check for existence
     * @return true if an entity with the specified identifier exists, false otherwise
     */
    boolean existsById(UUID id);

    /**
     * Counts the total number of events with the specified status.
     *
     * @param status the status of events to be counted
     * @return the number of events matching the given status
     */
    long countByStatus(EventStatus status);

    List<Event> findEventsReadyToOpenRegistration(LocalDateTime now);

    List<Event> findEventsReadyToCloseRegistration(LocalDateTime now);

    List<Event> findEventsReadyToBegin(LocalDateTime now);

    List<Event> findEventsReadyToComplete(LocalDateTime threshold);

    List<Event> findEventsNeedingReminder(LocalDateTime from, LocalDateTime to);
}
