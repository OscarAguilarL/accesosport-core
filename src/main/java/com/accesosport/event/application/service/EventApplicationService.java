package com.accesosport.event.application.service;

import com.accesosport.event.application.dto.CreateEventRequest;
import com.accesosport.event.application.dto.EventResponse;
import com.accesosport.event.application.dto.EventResponseMapper;
import com.accesosport.event.application.dto.EventSummaryResponse;
import com.accesosport.event.application.dto.UpdateEventRequest;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.event.domain.usecase.CancelEventUseCase;
import com.accesosport.event.domain.usecase.CompleteEventUseCase;
import com.accesosport.event.domain.usecase.CreateEventUseCase;
import com.accesosport.event.domain.usecase.ListAvailableEventsUseCase;
import com.accesosport.event.domain.usecase.ListEventsByOrganizerUseCase;
import com.accesosport.event.domain.usecase.OpenRegistrationUseCase;
import com.accesosport.event.domain.usecase.PublishEventUseCase;
import com.accesosport.event.domain.usecase.UpdateEventUseCase;
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
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventModalityRepository eventModalityRepository;
    private final RegistrationRepository registrationRepository;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public EventResponse createEvent(CreateEventRequest request, UUID organizerId) {
        List<CreateEventUseCase.ModalityData> modalityData = request.modalities().stream()
                .map(m -> new CreateEventUseCase.ModalityData(
                        m.name(), m.distance(), m.distanceUnit(), m.price(), m.capacity()))
                .toList();

        CreateEventUseCase.CreateEventCommand command = new CreateEventUseCase.CreateEventCommand(
                request.name(),
                request.description(),
                request.eventDate(),
                request.place(),
                request.city(),
                request.country(),
                request.latitude(),
                request.longitude(),
                request.registrationStartDate(),
                request.registrationEndDate(),
                modalityData,
                organizerId
        );

        CreateEventUseCase useCase = new CreateEventUseCase(eventRepository, userRepository, eventModalityRepository);
        CreateEventUseCase.CreateEventResult result = useCase.execute(command);

        return EventResponseMapper.toEventResponse(result.event(), result.modalities());
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
                request.registrationStartDate(),
                request.registrationEndDate(),
                request.waiverTemplate()
        );

        UpdateEventUseCase useCase = new UpdateEventUseCase(eventRepository);
        UpdateEventUseCase.UpdateEventResult result = useCase.execute(command);

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.event(), modalities);
    }

    @Transactional
    public EventResponse publishEvent(UUID eventId, UUID requesterId) {
        PublishEventUseCase useCase = new PublishEventUseCase(eventRepository, eventModalityRepository);
        PublishEventUseCase.PublishEventResult result = useCase.execute(
                new PublishEventUseCase.PublishEventCommand(eventId, requesterId));

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.event(), modalities);
    }

    @Transactional
    public EventResponse openRegistration(UUID eventId, UUID requesterId) {
        OpenRegistrationUseCase useCase = new OpenRegistrationUseCase(eventRepository);
        OpenRegistrationUseCase.OpenRegistrationResult result = useCase.execute(
                new OpenRegistrationUseCase.OpenRegistrationCommand(eventId, requesterId));

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.event(), modalities);
    }

    @Transactional
    public EventResponse cancelEvent(UUID eventId, String reason, UUID requesterId) {
        CancelEventUseCase useCase = new CancelEventUseCase(eventRepository, registrationRepository, domainEventPublisher);
        CancelEventUseCase.CancelEventResult result = useCase.execute(
                new CancelEventUseCase.CancelEventCommand(eventId, reason, requesterId));

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.canceledEvent(), modalities);
    }

    @Transactional
    public EventResponse completeEvent(UUID eventId, UUID requesterId) {
        CompleteEventUseCase useCase = new CompleteEventUseCase(eventRepository);
        CompleteEventUseCase.CompleteEventResult result = useCase.execute(
                new CompleteEventUseCase.CompleteEventCommand(eventId, requesterId));

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(result.event(), modalities);
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId));
        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        return EventResponseMapper.toEventResponse(event, modalities);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listAvailableEvents() {
        ListAvailableEventsUseCase useCase = new ListAvailableEventsUseCase(eventRepository);
        return toSummaryResponses(useCase.execute().events());
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> ListEventsByStatus(EventStatus status) {
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

    private List<EventSummaryResponse> toSummaryResponses(List<Event> events) {
        if (events.isEmpty()) return List.of();

        List<UUID> eventIds = events.stream().map(Event::getId).toList();
        Map<UUID, List<EventModality>> modalitiesByEvent = eventModalityRepository
                .findByEventIdIn(eventIds).stream()
                .collect(Collectors.groupingBy(EventModality::getEventId));

        return events.stream()
                .map(e -> EventResponseMapper.toEventSummaryResponse(
                        e, modalitiesByEvent.getOrDefault(e.getId(), List.of())))
                .toList();
    }
}
