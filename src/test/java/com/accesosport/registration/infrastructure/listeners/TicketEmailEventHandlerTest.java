package com.accesosport.registration.infrastructure.listeners;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.service.TicketPdfGenerator;
import com.accesosport.registration.domain.events.RegistrationConfirmedEvent;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.model.RegistrationStatus;
import com.accesosport.registration.domain.repository.RegistrationRepository;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketEmailEventHandlerTest {

    @Mock private TicketPdfGenerator ticketPdfGenerator;
    @Mock private EmailService emailService;
    @Mock private UserRepository userRepository;
    @Mock private EventRepository eventRepository;
    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventModalityRepository eventModalityRepository;
    @Mock private Event event;
    @Mock private Location location;
    @Mock private User user;

    private TicketEmailEventHandler handler;

    private UUID registrationId;
    private UUID eventId;
    private UUID participantId;
    private Registration registration;

    @BeforeEach
    void setUp() throws IOException {
        handler = new TicketEmailEventHandler(
                ticketPdfGenerator, emailService, userRepository,
                eventRepository, registrationRepository, eventModalityRepository
        );

        registrationId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        participantId = UUID.randomUUID();

        registration = Registration.reconstitute(
                registrationId, eventId, participantId, null, null,
                RegistrationStatus.CONFIRMED, "ACSP-TEST", null, null,
                false, null, null, null, null, null, true
        );

        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(userRepository.findById(participantId)).thenReturn(Optional.of(user));

        when(user.getEmail()).thenReturn("participant@test.com");
        when(user.getPersonalData()).thenReturn(PersonalData.builder().firstName("Ana").build());
        when(event.getName()).thenReturn("Maratón CDMX");
        when(event.getEventDate()).thenReturn(LocalDateTime.of(2026, 6, 15, 8, 0));
        when(event.getLocation()).thenReturn(location);
        when(location.place()).thenReturn("Chapultepec");
        when(location.city()).thenReturn("CDMX");

        when(ticketPdfGenerator.generate(any(), any(), any(), any())).thenReturn(new byte[]{1, 2, 3});
    }

    @Test
    void handle_shouldCallSendWithAttachmentWithPdfBytes() {
        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                registrationId, eventId, participantId, "ACSP-TEST", 42);

        handler.handle(domainEvent);

        ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(emailService).sendWithAttachment(anyString(), anyString(), anyString(), anyString(), contentCaptor.capture());
        assertThat(contentCaptor.getValue()).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void handle_emailShouldGoToParticipantEmail() {
        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                registrationId, eventId, participantId, "ACSP-TEST", null);

        handler.handle(domainEvent);

        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendWithAttachment(toCaptor.capture(), anyString(), anyString(), anyString(), any());
        assertThat(toCaptor.getValue()).isEqualTo("participant@test.com");
    }

    @Test
    void handle_subjectShouldContainEventName() {
        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                registrationId, eventId, participantId, "ACSP-TEST", null);

        handler.handle(domainEvent);

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendWithAttachment(anyString(), subjectCaptor.capture(), anyString(), anyString(), any());
        assertThat(subjectCaptor.getValue()).contains("Maratón CDMX");
    }

    @Test
    void handle_whenTicketPdfGeneratorThrows_shouldNotPropagateException() throws IOException {
        when(ticketPdfGenerator.generate(any(), any(), any(), any()))
                .thenThrow(new IOException("PDF generation failed"));

        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                registrationId, eventId, participantId, "ACSP-TEST", null);

        assertThatNoException().isThrownBy(() -> handler.handle(domainEvent));
        verify(emailService, never()).sendWithAttachment(any(), any(), any(), any(), any());
    }

    @Test
    void handle_whenUserNotFound_shouldNotSendEmailAndNotThrow() {
        when(userRepository.findById(participantId)).thenReturn(Optional.empty());

        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                registrationId, eventId, participantId, "ACSP-TEST", null);

        assertThatNoException().isThrownBy(() -> handler.handle(domainEvent));
        verify(emailService, never()).sendWithAttachment(any(), any(), any(), any(), any());
    }

    @Test
    void handle_whenEmailServiceThrows_shouldNotPropagateException() {
        doThrow(new RuntimeException("Resend error"))
                .when(emailService).sendWithAttachment(any(), any(), any(), any(), any());

        RegistrationConfirmedEvent domainEvent = new RegistrationConfirmedEvent(
                registrationId, eventId, participantId, "ACSP-TEST", null);

        assertThatNoException().isThrownBy(() -> handler.handle(domainEvent));
    }
}
