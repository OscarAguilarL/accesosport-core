package com.accesosport.registration.infrastructure.listeners;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.domain.events.RegistrationConfirmedEvent;
import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationConfirmationEmailHandlerTest {

    @Mock private EmailService emailService;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private Event event;
    @Mock private Location location;
    @Mock private User user;

    private RegistrationConfirmationEmailHandler handler;

    private UUID participantId;
    private UUID eventId;

    @BeforeEach
    void setUp() {
        handler = new RegistrationConfirmationEmailHandler(emailService, eventRepository, userRepository);
        participantId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        when(userRepository.findById(participantId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(user.getEmail()).thenReturn("participant@test.com");
        when(user.getPersonalData()).thenReturn(PersonalData.builder().firstName("Ana").build());
        when(event.getName()).thenReturn("Maratón CDMX");
        when(event.getEventDate()).thenReturn(LocalDateTime.of(2026, 6, 15, 8, 0));
        when(event.getLocation()).thenReturn(location);
        when(location.place()).thenReturn("Chapultepec");
        when(location.city()).thenReturn("CDMX");
    }

    @Test
    void handle_shouldSendEmailToParticipant() {
        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                UUID.randomUUID(), eventId, participantId, "ACSP-1234", null);

        handler.handle(domainEvent);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailService).send(captor.capture());
        assertThat(captor.getValue().to()).isEqualTo("participant@test.com");
        assertThat(captor.getValue().subject()).contains("Maratón CDMX");
    }

    @Test
    void handle_whenUserNotFound_shouldNotSendEmail() {
        when(userRepository.findById(participantId)).thenReturn(Optional.empty());

        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                UUID.randomUUID(), eventId, participantId, "ACSP-1234", null);

        handler.handle(domainEvent);

        verify(emailService, never()).send(any());
    }

    @Test
    void handle_whenEmailServiceThrows_shouldNotPropagateException() {
        doThrow(new RuntimeException("Resend error")).when(emailService).send(any());

        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                UUID.randomUUID(), eventId, participantId, "ACSP-1234", null);

        assertThatNoException().isThrownBy(() -> handler.handle(domainEvent));
    }

    @Test
    void handle_whenPersonalDataIsNull_shouldFallbackToParticipantName() {
        when(user.getPersonalData()).thenReturn(null);

        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                UUID.randomUUID(), eventId, participantId, "ACSP-9999", null);

        handler.handle(domainEvent);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailService).send(captor.capture());
        assertThat(captor.getValue().htmlBody()).contains("Participant");
    }
}
