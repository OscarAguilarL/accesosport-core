package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.events.EventCancelledEvent;
import com.accesosport.event.domain.exception.EventAccessDeniedException;
import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEvent;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CancelEventUseCaseTest {

    @Mock private EventRepository eventRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private DomainEventPublisher domainEventPublisher;
    @Mock private Event event;

    private CancelEventUseCase useCase;
    private UUID eventId;
    private UUID organizerId;

    @BeforeEach
    void setUp() {
        useCase = new CancelEventUseCase(eventRepository, registrationRepository, domainEventPublisher);
        eventId = UUID.randomUUID();
        organizerId = UUID.randomUUID();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(event.getId()).thenReturn(eventId);
        when(event.getName()).thenReturn("Maratón CDMX");
        when(event.getEventDate()).thenReturn(LocalDateTime.of(2026, 6, 15, 8, 0));

        com.accesosport.user.domain.model.User organizer = mock(com.accesosport.user.domain.model.User.class);
        when(organizer.getId()).thenReturn(organizerId);
        when(event.getCreatedBy()).thenReturn(organizer);
    }

    @Test
    void execute_shouldPublishEventCancelledEventWithAffectedIds() {
        UUID reg1 = UUID.randomUUID(), reg2 = UUID.randomUUID();
        Registration r1 = mockRegistration(reg1);
        Registration r2 = mockRegistration(reg2);
        when(registrationRepository.findConfirmedByEventId(eventId)).thenReturn(List.of(r1, r2));

        useCase.execute(new CancelEventUseCase.CancelEventCommand(eventId, "Razón", organizerId));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());
        EventCancelledEvent published = (EventCancelledEvent) captor.getValue();
        assertThat(published.getAffectedRegistrationIds()).containsExactlyInAnyOrder(reg1, reg2);
    }

    @Test
    void execute_shouldPublishEventWithCorrectReason() {
        when(registrationRepository.findConfirmedByEventId(eventId)).thenReturn(List.of());

        useCase.execute(new CancelEventUseCase.CancelEventCommand(eventId, "Lluvia extrema", organizerId));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());
        assertThat(((EventCancelledEvent) captor.getValue()).getCancellationReason())
                .isEqualTo("Lluvia extrema");
    }

    @Test
    void execute_whenNoConfirmedRegistrations_shouldPublishWithEmptyList() {
        when(registrationRepository.findConfirmedByEventId(eventId)).thenReturn(List.of());

        useCase.execute(new CancelEventUseCase.CancelEventCommand(eventId, "Razón", organizerId));

        ArgumentCaptor<DomainEvent> captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());
        assertThat(((EventCancelledEvent) captor.getValue()).getAffectedRegistrationIds()).isEmpty();
    }

    @Test
    void execute_whenEventNotFound_shouldThrowEventNotFoundException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute(new CancelEventUseCase.CancelEventCommand(eventId, "Razón", organizerId)))
                .isInstanceOf(EventNotFoundException.class);

        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void execute_whenRequesterIsNotOrganizer_shouldThrowEventAccessDeniedException() {
        UUID otherId = UUID.randomUUID();
        when(registrationRepository.findConfirmedByEventId(eventId)).thenReturn(List.of());

        assertThatThrownBy(() ->
                useCase.execute(new CancelEventUseCase.CancelEventCommand(eventId, "Razón", otherId)))
                .isInstanceOf(EventAccessDeniedException.class);

        verify(domainEventPublisher, never()).publish(any());
    }

    private Registration mockRegistration(UUID id) {
        Registration reg = mock(Registration.class);
        when(reg.getId()).thenReturn(id);
        return reg;
    }
}
