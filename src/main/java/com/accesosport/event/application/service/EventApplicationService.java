package com.accesosport.event.application.service;

import com.accesosport.event.application.dto.CreateEventRequest;
import com.accesosport.event.application.dto.EventResponse;
import com.accesosport.event.application.dto.EventResponseMapper;
import com.accesosport.event.application.dto.EventSummaryResponse;
import com.accesosport.event.application.dto.UpdateEventRequest;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCapacity;
import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.domain.repository.EventCapacityRepository;
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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventCapacityRepository eventCapacityRepository;

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

        CreateEventUseCase useCase = new CreateEventUseCase(eventRepository, userRepository, eventCapacityRepository);
        CreateEventUseCase.CreateEventResult result = useCase.execute(command);

        EventCapacity capacity = eventCapacityRepository.findByEventId(result.event().getId()).orElseThrow();
        return EventResponseMapper.toEventResponse(result.event(), capacity);
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

        UpdateEventUseCase useCase = new UpdateEventUseCase(eventRepository, eventCapacityRepository);
        UpdateEventUseCase.UpdateEventResult result = useCase.execute(command);

        EventCapacity capacity = eventCapacityRepository.findByEventId(eventId)
                .orElseGet(() -> EventCapacity.create(eventId, null));
        return EventResponseMapper.toEventResponse(result.event(), capacity);
    }

    @Transactional
    public EventResponse publishEvent(UUID eventId, UUID requesterId) {
        PublishEventUseCase useCase = new PublishEventUseCase(eventRepository);
        PublishEventUseCase.PublishEventResult result = useCase.execute(new PublishEventUseCase.PublishEventCommand(eventId, requesterId));

        EventCapacity capacity = eventCapacityRepository.findByEventId(eventId)
                .orElseGet(() -> EventCapacity.create(eventId, null));
        return EventResponseMapper.toEventResponse(result.event(), capacity);
    }

    @Transactional
    public EventResponse openRegistration(UUID eventId, UUID requesterId) {
        OpenRegistrationUseCase useCase = new OpenRegistrationUseCase(eventRepository);
        OpenRegistrationUseCase.OpenRegistrationResult result = useCase.execute(new OpenRegistrationUseCase.OpenRegistrationCommand(eventId, requesterId));

        EventCapacity capacity = eventCapacityRepository.findByEventId(eventId)
                .orElseGet(() -> EventCapacity.create(eventId, null));
        return EventResponseMapper.toEventResponse(result.event(), capacity);
    }

    @Transactional
    public EventResponse cancelEvent(UUID eventId, String reason, UUID requesterId) {
        CancelEventUseCase useCase = new CancelEventUseCase(eventRepository);
        CancelEventUseCase.CancelEventResult result = useCase.execute(new CancelEventUseCase.CancelEventCommand(eventId, reason, requesterId));

        EventCapacity capacity = eventCapacityRepository.findByEventId(eventId)
                .orElseGet(() -> EventCapacity.create(eventId, null));
        return EventResponseMapper.toEventResponse(result.canceledEvent(), capacity);
    }

    @Transactional
    public EventResponse completeEvent(UUID eventId, UUID requesterId) {
        CompleteEventUseCase useCase = new CompleteEventUseCase(eventRepository);
        CompleteEventUseCase.CompleteEventResult result = useCase.execute(new CompleteEventUseCase.CompleteEventCommand(eventId, requesterId));

        EventCapacity capacity = eventCapacityRepository.findByEventId(eventId)
                .orElseGet(() -> EventCapacity.create(eventId, null));
        return EventResponseMapper.toEventResponse(result.event(), capacity);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        EventCapacity capacity = eventCapacityRepository.findByEventId(eventId)
                .orElseGet(() -> EventCapacity.create(eventId, null));
        return EventResponseMapper.toEventResponse(event, capacity);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listAvailableEvents() {
        ListAvailableEventsUseCase useCase = new ListAvailableEventsUseCase(eventRepository);
        ListAvailableEventsUseCase.ListAvailableEventsResult result = useCase.execute();

        return toSummaryResponsesWithCapacity(result.events());
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> ListEventsByStatus(EventStatus status) {
        List<Event> events = eventRepository.findByStatus(status);
        return toSummaryResponsesWithCapacity(events);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listEventsByOrganizerId(UUID organizerId) {
        ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand command = new ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand(organizerId);
        ListEventsByOrganizerUseCase useCase = new ListEventsByOrganizerUseCase(eventRepository);
        ListEventsByOrganizerUseCase.ListEventsByOrganizerResult result = useCase.execute(command);

        return toSummaryResponsesWithCapacity(result.events());
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inThreeMonths = now.plusMonths(3);
        List<Event> events = eventRepository.findUpcomingEvents(now, inThreeMonths);
        return toSummaryResponsesWithCapacity(events);
    }

    private List<EventSummaryResponse> toSummaryResponsesWithCapacity(List<Event> events) {
        if (events.isEmpty()) return List.of();

        List<UUID> eventIds = events.stream().map(Event::getId).toList();
        Map<UUID, EventCapacity> capacities = eventCapacityRepository.findAllByEventIdIn(eventIds).stream()
                .collect(Collectors.toMap(EventCapacity::getEventId, c -> c));

        return events.stream()
                .map(e -> EventResponseMapper.toEventSummaryResponse(e,
                        capacities.getOrDefault(e.getId(), EventCapacity.create(e.getId(), null))))
                .toList();
    }
}
