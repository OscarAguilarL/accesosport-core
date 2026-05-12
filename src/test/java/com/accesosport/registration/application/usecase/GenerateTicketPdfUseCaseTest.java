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
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerateTicketPdfUseCaseTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventModalityRepository eventModalityRepository;
    @Mock private TicketPdfGenerator ticketPdfGenerator;
    @Mock private Event event;
    @Mock private Location location;

    private GenerateTicketPdfUseCase useCase;
    private UUID registrationId;
    private UUID participantId;
    private UUID eventId;

    @BeforeEach
    void setUp() throws IOException {
        useCase = new GenerateTicketPdfUseCase(
                registrationRepository, eventRepository, userRepository, eventModalityRepository, ticketPdfGenerator);
        registrationId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        eventId = UUID.randomUUID();

        when(event.getId()).thenReturn(eventId);
        when(event.getName()).thenReturn("Maratón CDMX 2026");
        when(event.getEventDate()).thenReturn(LocalDateTime.of(2026, 6, 15, 8, 0));
        when(event.getLocation()).thenReturn(location);
        when(location.place()).thenReturn("Av. Reforma, CDMX");
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        User participant = User.builder()
                .id(participantId)
                .email("juan@test.com")
                .personalData(PersonalData.builder()
                        .firstName("Juan")
                        .lastName("García")
                        .build())
                .build();
        when(userRepository.findById(participantId)).thenReturn(Optional.of(participant));

        when(ticketPdfGenerator.generate(any(), any(), any(), any())).thenReturn(new byte[]{1, 2, 3});
    }

    @Test
    void execute_whenRegistrationNotFound_shouldThrowRegistrationNotFoundException() throws Exception {
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new GenerateTicketPdfUseCase.Command(registrationId, participantId)))
                .isInstanceOf(RegistrationNotFoundException.class);

        verify(ticketPdfGenerator, never()).generate(any(), any(), any(), any());
    }

    @Test
    void execute_whenRequesterIsNotOwner_shouldThrowRegistrationAccessDeniedException() throws Exception {
        UUID otherId = UUID.randomUUID();
        Registration registration = confirmedRegistration();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> useCase.execute(new GenerateTicketPdfUseCase.Command(registrationId, otherId)))
                .isInstanceOf(RegistrationAccessDeniedException.class);

        verify(ticketPdfGenerator, never()).generate(any(), any(), any(), any());
    }

    @Test
    void execute_whenRegistrationIsCancelled_shouldThrowRegistrationNotConfirmedException() throws Exception {
        Registration registration = registrationWithStatus(RegistrationStatus.CANCELLED);
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> useCase.execute(new GenerateTicketPdfUseCase.Command(registrationId, participantId)))
                .isInstanceOf(RegistrationNotConfirmedException.class);

        verify(ticketPdfGenerator, never()).generate(any(), any(), any(), any());
    }

    @Test
    void execute_whenRegistrationIsPendingPayment_shouldThrowRegistrationNotConfirmedException() {
        Registration registration = registrationWithStatus(RegistrationStatus.PENDING_PAYMENT);
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> useCase.execute(new GenerateTicketPdfUseCase.Command(registrationId, participantId)))
                .isInstanceOf(RegistrationNotConfirmedException.class);
    }

    @Test
    void execute_whenAllValid_shouldReturnPdfBytes() throws Exception {
        Registration registration = confirmedRegistration();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));

        byte[] result = useCase.execute(new GenerateTicketPdfUseCase.Command(registrationId, participantId));

        assertThat(result).isNotNull().isNotEmpty();
        verify(ticketPdfGenerator).generate(eq(registration), eq(event), any(User.class), any());
    }

    @Test
    void execute_whenPdfGeneratorThrows_shouldWrapInRuntimeException() throws Exception {
        Registration registration = confirmedRegistration();
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));
        when(ticketPdfGenerator.generate(any(), any(), any(), any())).thenThrow(new IOException("PDFBox error"));

        assertThatThrownBy(() -> useCase.execute(new GenerateTicketPdfUseCase.Command(registrationId, participantId)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("PDF");
    }

    private Registration confirmedRegistration() {
        return registrationWithStatus(RegistrationStatus.CONFIRMED);
    }

    private Registration registrationWithStatus(RegistrationStatus status) {
        return Registration.reconstitute(
                registrationId, eventId, participantId, null,
                status, "ACSP-TEST", 42, null, false, null,
                LocalDateTime.now(), null, null, null, true);
    }
}
