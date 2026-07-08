package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventCapacityRepository;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.dto.CancelRegistrationCommand;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.domain.exception.RegistrationAccessDeniedException;
import com.accesosport.registration.domain.exception.RegistrationNotFoundException;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.model.RegistrationStatus;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CancelRegistrationUseCaseTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventModalityRepository eventModalityRepository;
    @Mock private EventCapacityRepository eventCapacityRepository;
    @Mock private EventRepository eventRepository;
    @Mock private DomainEventPublisher domainEventPublisher;
    @Mock private Registration registration;
    @Mock private Event event;

    private CancelRegistrationUseCase useCase;
    private UUID registrationId;
    private UUID eventId;
    private UUID participantId;
    private UUID modalityId;

    @BeforeEach
    void setUp() {
        useCase = new CancelRegistrationUseCase(
                registrationRepository, eventModalityRepository, eventCapacityRepository,
                eventRepository, domainEventPublisher
        );
        registrationId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        modalityId = UUID.randomUUID();

        when(registrationRepository.findById(registrationId)).thenReturn(Optional.of(registration));
        when(registrationRepository.save(any(Registration.class))).thenReturn(registration);
        when(registration.getId()).thenReturn(registrationId);
        when(registration.getEventId()).thenReturn(eventId);
        when(registration.getParticipantId()).thenReturn(participantId);
        when(registration.getStatus()).thenReturn(RegistrationStatus.CONFIRMED);
        when(registration.getTicketCode()).thenReturn("ACSP-1234");
        when(registration.getModalityId()).thenReturn(modalityId);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(event.getEventDate()).thenReturn(LocalDateTime.now().plusDays(30));
    }

    @Test
    void cancelacion_liberaCupoGlobalDelEvento() {
        useCase.execute(new CancelRegistrationCommand(registrationId, participantId, false));

        verify(eventCapacityRepository).release(eventId);
    }

    @Test
    void cancelacion_decrementaConteoDeModalidad() {
        useCase.execute(new CancelRegistrationCommand(registrationId, participantId, false));

        verify(eventModalityRepository).release(modalityId);
    }

    @Test
    void cancelacion_publicaDomainEvent() {
        useCase.execute(new CancelRegistrationCommand(registrationId, participantId, false));

        verify(domainEventPublisher).publish(any());
    }

    @Test
    void cancelacion_guardaRegistrationActualizado() {
        useCase.execute(new CancelRegistrationCommand(registrationId, participantId, false));

        verify(registrationRepository).save(registration);
    }

    @Test
    void cancelacion_retornaResponse() {
        RegistrationResponse response = useCase.execute(new CancelRegistrationCommand(registrationId, participantId, false));

        assertThat(response).isNotNull();
    }

    @Test
    void adminPuedeCancelarCualquierRegistration() {
        UUID adminId = UUID.randomUUID();

        useCase.execute(new CancelRegistrationCommand(registrationId, adminId, true));

        verify(eventCapacityRepository).release(eventId);
    }

    @Test
    void sinModalityId_noLlamaReleaseEnModalidad() {
        when(registration.getModalityId()).thenReturn(null);

        useCase.execute(new CancelRegistrationCommand(registrationId, participantId, false));

        verify(eventModalityRepository, never()).release(any());
        verify(eventCapacityRepository).release(eventId);
    }

    @Test
    void registrationNoEncontrada_lanzaRegistrationNotFoundException() {
        when(registrationRepository.findById(registrationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute(new CancelRegistrationCommand(registrationId, participantId, false)))
                .isInstanceOf(RegistrationNotFoundException.class);

        verify(eventCapacityRepository, never()).release(any());
    }

    @Test
    void participanteNoEsDueno_lanzaRegistrationAccessDeniedException() {
        UUID otherParticipant = UUID.randomUUID();

        assertThatThrownBy(() ->
                useCase.execute(new CancelRegistrationCommand(registrationId, otherParticipant, false)))
                .isInstanceOf(RegistrationAccessDeniedException.class);

        verify(eventCapacityRepository, never()).release(any());
    }

    @Test
    void adminPuedeCancelarDeOtroParticipante_sinErrorDeAcceso() {
        UUID adminId = UUID.randomUUID();

        useCase.execute(new CancelRegistrationCommand(registrationId, adminId, true));

        verify(eventModalityRepository).release(modalityId);
    }
}
