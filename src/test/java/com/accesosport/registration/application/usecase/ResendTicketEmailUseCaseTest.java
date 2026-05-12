package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.service.TicketPdfGenerator;
import com.accesosport.registration.domain.exception.RegistrationAccessDeniedException;
import com.accesosport.registration.domain.exception.RegistrationNotConfirmedException;
import com.accesosport.registration.domain.exception.RegistrationNotFoundException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResendTicketEmailUseCaseTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventModalityRepository eventModalityRepository;
    @Mock private TicketPdfGenerator ticketPdfGenerator;
    @Mock private EmailService emailService;
    @Mock private Event event;
    @Mock private Location location;

    private ResendTicketEmailUseCase useCase;

    private UUID registrationId;
    private UUID participantId;
    private UUID eventId;

    @BeforeEach
    void setUp() throws IOException {
        useCase = new ResendTicketEmailUseCase(
                registrationRepository, eventRepository, userRepository,
                eventModalityRepository, ticketPdfGenerator, emailService
        );

        registrationId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        when(event.getName()).thenReturn("Maratón CDMX");
        when(event.getEventDate()).thenReturn(LocalDateTime.of(2026, 6, 15, 8, 0));
        when(event.getLocation()).thenReturn(location);
        when(location.place()).thenReturn("Chapultepec");
        when(location.city()).thenReturn("CDMX");
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        User participant = User.builder()
                .id(participantId)
                .email("participant@test.com")
                .personalData(PersonalData.builder().firstName("Ana").build())
                .build();
        when(userRepository.findById(participantId)).thenReturn(Optional.of(participant));

        when(ticketPdfGenerator.generate(any(), any(), any(), any())).thenReturn(new byte[]{1, 2, 3});
    }

    @Test
    void execute_whenRegistrationNotFound_shouldThrowRegistrationNotFoundException() {
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new ResendTicketEmailUseCase.Command(registrationId, participantId)))
                .isInstanceOf(RegistrationNotFoundException.class);

        verify(emailService, never()).sendWithAttachment(any(), any(), any(), any(), any());
    }

    @Test
    void execute_whenRequesterIsNotOwner_shouldThrowRegistrationAccessDeniedException() {
        UUID otherId = UUID.randomUUID();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(confirmedRegistration()));

        assertThatThrownBy(() -> useCase.execute(new ResendTicketEmailUseCase.Command(registrationId, otherId)))
                .isInstanceOf(RegistrationAccessDeniedException.class);

        verify(emailService, never()).sendWithAttachment(any(), any(), any(), any(), any());
    }

    @Test
    void execute_whenRegistrationNotConfirmed_shouldThrowRegistrationNotConfirmedException() {
        when(registrationRepository.findById(registrationId))
                .thenReturn(Optional.of(registrationWithStatus(RegistrationStatus.PENDING_PAYMENT)));

        assertThatThrownBy(() -> useCase.execute(new ResendTicketEmailUseCase.Command(registrationId, participantId)))
                .isInstanceOf(RegistrationNotConfirmedException.class);

        verify(emailService, never()).sendWithAttachment(any(), any(), any(), any(), any());
    }

    @Test
    void execute_happyPath_shouldCallSendWithAttachmentWithPdf() {
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(confirmedRegistration()));

        useCase.execute(new ResendTicketEmailUseCase.Command(registrationId, participantId));

        ArgumentCaptor<byte[]> contentCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(emailService).sendWithAttachment(
                eq("participant@test.com"),
                anyString(),
                anyString(),
                anyString(),
                contentCaptor.capture()
        );
        assertThat(contentCaptor.getValue()).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    void execute_happyPath_subjectShouldContainEventName() {
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(confirmedRegistration()));

        useCase.execute(new ResendTicketEmailUseCase.Command(registrationId, participantId));

        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendWithAttachment(anyString(), subjectCaptor.capture(), anyString(), anyString(), any());
        assertThat(subjectCaptor.getValue()).contains("Maratón CDMX");
    }

    private Registration confirmedRegistration() {
        return registrationWithStatus(RegistrationStatus.CONFIRMED);
    }

    private Registration registrationWithStatus(RegistrationStatus status) {
        return Registration.reconstitute(
                registrationId, eventId, participantId, null,
                status, "ACSP-TEST", 42, null, false, null,
                LocalDateTime.now(), null, null, null
        );
    }
}
