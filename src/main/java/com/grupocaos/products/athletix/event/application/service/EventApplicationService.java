package com.grupocaos.products.athletix.event.application.service;

import com.grupocaos.products.athletix.event.application.dto.CreateEventRequest;
import com.grupocaos.products.athletix.event.application.dto.EventResponse;
import com.grupocaos.products.athletix.event.application.dto.EventResponseMapper;
import com.grupocaos.products.athletix.event.application.dto.EventSummaryResponse;
import com.grupocaos.products.athletix.event.domain.exception.EventNotFoundException;
import com.grupocaos.products.athletix.event.domain.model.Event;
import com.grupocaos.products.athletix.event.domain.model.EventStatus;
import com.grupocaos.products.athletix.event.domain.repository.EventRepository;
import com.grupocaos.products.athletix.event.domain.usecase.CancelEventUseCase;
import com.grupocaos.products.athletix.event.domain.usecase.CreateEventUseCase;
import com.grupocaos.products.athletix.event.domain.usecase.ListAvailableEventsUseCase;
import com.grupocaos.products.athletix.event.domain.usecase.ListEventsByOrganizerUseCase;
import com.grupocaos.products.athletix.event.domain.usecase.OpenRegistrationUseCase;
import com.grupocaos.products.athletix.event.domain.usecase.PublishEventUseCase;
import com.grupocaos.products.athletix.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventApplicationService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;

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
                request.latitude(),
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
    public EventResponse publishEvent(UUID eventId) {
        PublishEventUseCase useCase = new PublishEventUseCase(eventRepository);
        PublishEventUseCase.PublishEventResult result = useCase.execute(new PublishEventUseCase.PublishEventCommand(eventId));

        return EventResponseMapper.toEventResponse(result.event());
    }

    @Transactional
    public EventResponse openRegistration(UUID eventId) {
        OpenRegistrationUseCase useCase = new OpenRegistrationUseCase(eventRepository);
        OpenRegistrationUseCase.OpenRegistrationResult result = useCase.execute(new OpenRegistrationUseCase.OpenRegistrationCommand(eventId));

        return EventResponseMapper.toEventResponse(result.event());
    }

    @Transactional
    public EventResponse cancelEvent(UUID eventId, String reason) {
        CancelEventUseCase useCase = new CancelEventUseCase(eventRepository);
        CancelEventUseCase.CancelEventResult result = useCase.execute(new CancelEventUseCase.CancelEventCommand(eventId, reason));

        return EventResponseMapper.toEventResponse(result.canceledEvent());
    }

    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        return EventResponseMapper.toEventResponse(event);
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listAvailableEvents() {
        ListAvailableEventsUseCase useCase = new ListAvailableEventsUseCase(eventRepository);
        ListAvailableEventsUseCase.ListAvailableEventsResult result = useCase.execute(null);

        return result.events().stream()
                .map(EventResponseMapper::toEventSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> ListEventsByStatus(EventStatus status) {
        List<Event> events = eventRepository.findByStatus(status);

        return events.stream()
                .map(EventResponseMapper::toEventSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EventSummaryResponse> listEventsByOrganizerId(UUID organizerId) {
        ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand command = new ListEventsByOrganizerUseCase.ListEventsByOrganizerCommand(organizerId);
        ListEventsByOrganizerUseCase useCase = new ListEventsByOrganizerUseCase(eventRepository);

        ListEventsByOrganizerUseCase.ListEventsByOrganizerResult result = useCase.execute(command);

        return result.events().stream()
                .map(EventResponseMapper::toEventSummaryResponse)
                .toList();
    }

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
