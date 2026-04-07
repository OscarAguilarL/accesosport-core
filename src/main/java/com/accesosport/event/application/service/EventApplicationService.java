package com.accesosport.event.application.service;

import com.accesosport.event.application.dto.CreateEventRequest;
import com.accesosport.event.application.dto.EventResponse;
import com.accesosport.event.application.dto.EventResponseMapper;
import com.accesosport.event.application.dto.EventSummaryResponse;
import com.accesosport.event.application.dto.UpdateEventRequest;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.event.domain.usecase.CancelEventUseCase;
import com.accesosport.event.domain.usecase.CompleteEventUseCase;
import com.accesosport.event.domain.usecase.CreateEventUseCase;
import com.accesosport.event.domain.usecase.ListAvailableEventsUseCase;
import com.accesosport.event.domain.usecase.ListEventsByOrganizerUseCase;
import com.accesosport.event.domain.usecase.OpenRegistrationUseCase;
import com.accesosport.event.domain.usecase.PublishEventUseCase;
import com.accesosport.event.domain.usecase.UpdateEventUseCase;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service class for managing events.
 *
 * <p>
 * This service interacts with repositories and use cases to perform operations on events.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new event based on the provided request and associates it with the specified organizer.
     *
     * @param request     the details of the event to be created, such as name, description, date, location, and other attributes
     * @param organizerId the unique identifier of the organizer creating the event
     * @return an {@code EventResponse} containing the details of the newly created event
     */
    @Transactional
    public EventResponse createEvent(CreateEventRequest request, UUID organizerId) {
        CreateEventUseCase.CreateEventCommand command = new CreateEventUseCase.CreateEventCommand(
                request.name(),
                request.description(),
                request.eventDate(),
                request.place(),
                request.city(),
                request.country(),
                request.latitude(),
                request.longitude(),
                request.raceType(),
                request.distance(),
                request.distanceUnit(),
                request.price(),
                request.registrationStartDate(),
                request.registrationEndDate(),
                request.maxParticipants(),
                organizerId
        );

        CreateEventUseCase useCase = new CreateEventUseCase(eventRepository, userRepository);
        CreateEventUseCase.CreateEventResult result = useCase.execute(command);

        return EventResponseMapper.toEventResponse(result.event());
    }

    @Transactional
    public EventResponse updateEvent(UUID eventId, UpdateEventRequest request, UUID requesterId) {
        UpdateEventUseCase.UpdateEventCommand command = new UpdateEventUseCase.UpdateEventCommand(
                eventId,
                requesterId,
                request.name(),
                request.description(),
                request.eventDate(),
                request.place(),
                request.city(),
                request.country(),
                request.latitude(),
                request.longitude(),
                request.raceType(),
                request.distance(),
                request.distanceUnit(),
                request.price(),
                request.registrationStartDate(),
                request.registrationEndDate(),
                request.maxParticipants()
        );

        UpdateEventUseCase useCase = new UpdateEventUseCase(eventRepository);
        UpdateEventUseCase.UpdateEventResult result = useCase.execute(command);

        return EventResponseMapper.toEventResponse(result.event());
    }

    /**
     * Publishes an event with the specified event ID, updating its status and making it publicly available.
     *
     * @param eventId the unique identifier of the event that needs to be published
     * @return an {@code EventResponse} containing the details of the published event
     */
    @Transactional
    public EventResponse publishEvent(UUID eventId, UUID requesterId) {
        PublishEventUseCase useCase = new PublishEventUseCase(eventRepository);
        PublishEventUseCase.PublishEventResult result = useCase.execute(new PublishEventUseCase.PublishEventCommand(eventId, requesterId));

        return EventResponseMapper.toEventResponse(result.event());
    }

    /**
     * Opens the registration for an event identified by the given event ID.
     *
     * @param eventId the unique identifier of the event for which the registrations will be opened
     * @return an {@code EventResponse} containing the details of the event with updated registration status
     */
    @Transactional
    public EventResponse openRegistration(UUID eventId, UUID requesterId) {
        OpenRegistrationUseCase useCase = new OpenRegistrationUseCase(eventRepository);
        OpenRegistrationUseCase.OpenRegistrationResult result = useCase.execute(new OpenRegistrationUseCase.OpenRegistrationCommand(eventId, requesterId));

        return EventResponseMapper.toEventResponse(result.event());
    }

    /**
     * Cancels an event identified by the given event ID, providing a reason for the cancellation.
     * Updates the event status to reflect the cancellation.
     *
     * @param eventId the unique identifier of the event to be canceled
     * @param reason  the reason for canceling the event
     * @return an {@code EventResponse} containing the details of the canceled event
     */
    @Transactional
    public EventResponse cancelEvent(UUID eventId, String reason, UUID requesterId) {
        CancelEventUseCase useCase = new CancelEventUseCase(eventRepository);
        CancelEventUseCase.CancelEventResult result = useCase.execute(new CancelEventUseCase.CancelEventCommand(eventId, reason, requesterId));

        return EventResponseMapper.toEventResponse(result.canceledEvent());
    }

    @Transactional
    public EventResponse completeEvent(UUID eventId, UUID requesterId) {
        CompleteEventUseCase useCase = new CompleteEventUseCase(eventRepository);
        CompleteEventUseCase.CompleteEventResult result = useCase.execute(new CompleteEventUseCase.CompleteEventCommand(eventId, requesterId));

        return EventResponseMapper.toEventResponse(result.event());
    }

    /**
     * Retrieves the details of an event identified by the provided event ID.
     * If the event is not found, an {@code EventNotFoundException} is thrown.
     *
     * @param eventId the unique identifier of the event to be retrieved
     * @return an {@code EventResponse} object containing the details of the requested event
     * @throws EventNotFoundException if no event is found for the provided event ID
     */
    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        return EventResponseMapper.toEventResponse(event);
    }

    /**
     * Retrieves a list of available events.
     * <p>
     * This method uses the {@code ListAvailableEventsUseCase} to fetch events that are currently available
     * and maps the results to a list of {@code EventSummaryResponse}.
     * The data retrieval process is read-only as it does not modify any state.
     * </p>
     *
     * @return a list of {@code EventSummaryResponse} objects representing the available events
     */
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listAvailableEvents() {
        ListAvailableEventsUseCase useCase = new ListAvailableEventsUseCase(eventRepository);
        ListAvailableEventsUseCase.ListAvailableEventsResult result = useCase.execute();

        return result.events().stream()
                .map(EventResponseMapper::toEventSummaryResponse)
                .toList();
    }

    /**
     * Retrieves a list of events corresponding to the specified status.
     * This method queries the repository for events matching the provided status
     * and maps them to a list of {@code EventSummaryResponse}.
     *
     * @param status the status of events to filter, such as PUBLISHED, PENDING, or CANCELED
     * @return a list of {@code EventSummaryResponse} objects representing the events with the specified status
     */
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> ListEventsByStatus(EventStatus status) {
        List<Event> events = eventRepository.findByStatus(status);

        return events.stream()
                .map(EventResponseMapper::toEventSummaryResponse)
                .toList();
    }

    /**
     * Retrieves a list of events organized by the specified organizer.
     * This method uses the {@code ListEventsByOrganizerUseCase} to query the database
     * for events that are associated with the given organizer ID and maps the results
     * to a list of {@code EventSummaryResponse}.
     *
     * @param organizerId the unique identifier of the organizer whose events are to be retrieved
     * @return a list of {@code EventSummaryResponse} objects representing the events organized by the specific organizer
     */
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listEventsByOrganizerId(UUID organizerId) {
        ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand command = new ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand(organizerId);
        ListEventsByOrganizerUseCase useCase = new ListEventsByOrganizerUseCase(eventRepository);

        ListEventsByOrganizerUseCase.ListEventsByOrganizerResult result = useCase.execute(command);

        return result.events().stream()
                .map(EventResponseMapper::toEventSummaryResponse)
                .toList();
    }

    /**
     * Retrieves a list of upcoming events scheduled within the next three months from the current time.
     * This method queries the repository for events occurring in this range and maps them to a list of
     * {@code EventSummaryResponse}.
     *
     * @return a list of {@code EventSummaryResponse} objects representing the events scheduled to occur
     * within the specified upcoming timeframe.
     */
    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inThreeMonths = now.plusMonths(3);

        List<Event> events = eventRepository.findUpcomingEvents(now, inThreeMonths);

        return events.stream()
                .map(EventResponseMapper::toEventSummaryResponse)
                .toList();
    }
}
