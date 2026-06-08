package com.accesosport.event.application.service;

import com.accesosport.event.application.dto.CreateEventRequest;
import com.accesosport.event.application.dto.EventResponse;
import com.accesosport.event.application.dto.EventResponseMapper;
import com.accesosport.event.application.dto.EventSummaryResponse;
import com.accesosport.event.application.dto.UpdateEventRequest;
import com.accesosport.shared.domain.query.PageQuery;
import com.accesosport.shared.domain.query.PageResult;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCapacity;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.domain.repository.EventCapacityRepository;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.event.application.usecase.CancelEventUseCase;
import com.accesosport.event.application.usecase.CompleteEventUseCase;
import com.accesosport.event.application.usecase.CreateEventUseCase;
import com.accesosport.event.application.usecase.ListAvailableEventsUseCase;
import com.accesosport.event.application.usecase.ListEventsByOrganizerUseCase;
import com.accesosport.event.application.usecase.OpenRegistrationUseCase;
import com.accesosport.event.application.usecase.PublishEventUseCase;
import com.accesosport.event.application.usecase.UpdateEventUseCase;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import com.accesosport.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventModalityRepository eventModalityRepository;
    private final EventCapacityRepository eventCapacityRepository;
    private final RegistrationRepository registrationRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request, UUID organizerId) {
        List<CreateEventUseCase.ModalityData> modalityData = request.modalities().stream()
                .map(m -> new CreateEventUseCase.ModalityData(
                        m.name(), m.distance(), m.distanceUnit(), m.price()))
                .toList();

        CreateEventUseCase.CreateEventCommand command = new CreateEventUseCase.CreateEventCommand(
                request.name(),
                request.description(),
                request.eventDate(),
                request.place(),
                request.city(),
                request.country(),
                request.registrationStartDate(),
                request.registrationEndDate(),
                request.maxCapacity(),
                modalityData,
                organizerId
        );

        CreateEventUseCase useCase = new CreateEventUseCase(eventRepository, userRepository, eventModalityRepository, eventCapacityRepository);
        CreateEventUseCase.CreateEventResult result = useCase.execute(command);

        Optional<EventCapacity> capacity = eventCapacityRepository.findByEventId(result.event().getId());
        return EventResponseMapper.toEventResponse(result.event(), result.modalities(), capacity);
    }

    @Transactional
    public EventResponse updateEvent(UUID eventId, UpdateEventRequest request, UUID userId, boolean isAdmin) {
        UUID requesterId = isAdmin ? null : userId;
        UpdateEventUseCase.UpdateEventCommand command = new UpdateEventUseCase.UpdateEventCommand(
                eventId,
                requesterId,
                request.name(),
                request.description(),
                request.eventDate(),
                request.place(),
                request.city(),
                request.country(),
                request.registrationStartDate(),
                request.registrationEndDate(),
                request.waiverTemplate()
        );

        UpdateEventUseCase useCase = new UpdateEventUseCase(eventRepository);
        UpdateEventUseCase.UpdateEventResult result = useCase.execute(command);

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        Optional<EventCapacity> capacity = eventCapacityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.event(), modalities, capacity);
    }

    @Transactional
    public EventResponse publishEvent(UUID eventId, UUID userId, boolean isAdmin) {
        UUID requesterId = isAdmin ? null : userId;
        PublishEventUseCase useCase = new PublishEventUseCase(eventRepository, eventModalityRepository);
        PublishEventUseCase.PublishEventResult result = useCase.execute(
                new PublishEventUseCase.PublishEventCommand(eventId, requesterId));

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        Optional<EventCapacity> capacity = eventCapacityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.event(), modalities, capacity);
    }

    @Transactional
    public EventResponse openRegistration(UUID eventId, UUID userId, boolean isAdmin) {
        UUID requesterId = isAdmin ? null : userId;
        OpenRegistrationUseCase useCase = new OpenRegistrationUseCase(eventRepository);
        OpenRegistrationUseCase.OpenRegistrationResult result = useCase.execute(
                new OpenRegistrationUseCase.OpenRegistrationCommand(eventId, requesterId));

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        Optional<EventCapacity> capacity = eventCapacityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.event(), modalities, capacity);
    }

    @Transactional
    public EventResponse cancelEvent(UUID eventId, String reason, UUID userId, boolean isAdmin) {
        UUID requesterId = isAdmin ? null : userId;
        CancelEventUseCase useCase = new CancelEventUseCase(eventRepository, registrationRepository, domainEventPublisher);
        CancelEventUseCase.CancelEventResult result = useCase.execute(
                new CancelEventUseCase.CancelEventCommand(eventId, reason, requesterId));

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        Optional<EventCapacity> capacity = eventCapacityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.canceledEvent(), modalities, capacity);
    }

    @Transactional
    public EventResponse completeEvent(UUID eventId, UUID userId, boolean isAdmin) {
        UUID requesterId = isAdmin ? null : userId;
        CompleteEventUseCase useCase = new CompleteEventUseCase(eventRepository);
        CompleteEventUseCase.CompleteEventResult result = useCase.execute(
                new CompleteEventUseCase.CompleteEventCommand(eventId, requesterId));

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        Optional<EventCapacity> capacity = eventCapacityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.event(), modalities, capacity);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        Optional<EventCapacity> capacity = eventCapacityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(event, modalities, capacity);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listAvailableEvents() {
        ListAvailableEventsUseCase useCase = new ListAvailableEventsUseCase(eventRepository);
        return toSummaryResponses(useCase.execute().events());
    }

    public List<EventSummaryResponse> listEvents(EventStatus status) {
        if (status != null) {
            return listEventsByStatus(status);
        }
        return listUpcomingEvents();
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listEventsByStatus(EventStatus status) {
        return toSummaryResponses(eventRepository.findByStatus(status));
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listEventsByOrganizerId(UUID organizerId) {
        ListEventsByOrganizerUseCase useCase = new ListEventsByOrganizerUseCase(eventRepository);
        ListEventsByOrganizerUseCase.ListEventsByOrganizerResult result = useCase.execute(
                new ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand(organizerId));
        return toSummaryResponses(result.events());
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        return toSummaryResponses(eventRepository.findUpcomingEvents(now, now.plusMonths(3)));
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listPublishedEvents() {
        return toSummaryResponses(eventRepository.findByStatus(EventStatus.PUBLISHED));
    }

    @Transactional(readOnly = true)
    public PageResult<EventSummaryResponse> listPublishedEventsPaged(PageQuery query) {
        return toPagedSummaryResponses(eventRepository.findByStatus(EventStatus.PUBLISHED, query));
    }

    @Transactional(readOnly = true)
    public PageResult<EventSummaryResponse> listAvailableEventsPaged(PageQuery query) {
        PageResult<Event> result = eventRepository.findEventsAvailableForRegistration(query);
        return toPagedSummaryResponses(result);
    }

    @Transactional(readOnly = true)
    public PageResult<EventSummaryResponse> listEventsByStatusPaged(EventStatus status, PageQuery query) {
        PageResult<Event> result = eventRepository.findByStatus(status, query);
        return toPagedSummaryResponses(result);
    }

    @Transactional(readOnly = true)
    public PageResult<EventSummaryResponse> listMyEventsPaged(UUID organizerId, PageQuery query) {
        PageResult<Event> result = eventRepository.findByOrganizerId(organizerId, query);
        return toPagedSummaryResponses(result);
    }

    @Transactional(readOnly = true)
    public PageResult<EventSummaryResponse> listEventsPaged(EventStatus status, PageQuery query) {
        if (status != null) {
            return listEventsByStatusPaged(status, query);
        }
        return toPagedSummaryResponses(eventRepository.findAll(query));
    }

    private List<EventSummaryResponse> toSummaryResponses(List<Event> events) {
        if (events.isEmpty()) return List.of();

        List<UUID> eventIds = events.stream().map(Event::getId).toList();

        Map<UUID, List<EventModality>> modalitiesByEvent = eventModalityRepository
                .findByEventIdIn(eventIds).stream()
                .collect(Collectors.groupingBy(EventModality::getEventId));

        Map<UUID, EventCapacity> capacityByEvent = eventCapacityRepository
                .findByEventIdIn(eventIds).stream()
                .collect(Collectors.toMap(EventCapacity::getEventId, c -> c));

        return events.stream()
                .map(e -> EventResponseMapper.toEventSummaryResponse(
                        e,
                        modalitiesByEvent.getOrDefault(e.getId(), List.of()),
                        Optional.ofNullable(capacityByEvent.get(e.getId()))
                ))
                .toList();
    }

    private PageResult<EventSummaryResponse> toPagedSummaryResponses(PageResult<Event> pageResult) {
        List<EventSummaryResponse> content = toSummaryResponses(pageResult.content());
        return PageResult.of(content, pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
