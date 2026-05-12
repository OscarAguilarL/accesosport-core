package com.accesosport.registration.application.usecase;

import com.accesosport.event.domain.model.DistanceUnit;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.model.EventStatus;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.registration.application.dto.RegisterParticipantCommand;
import com.accesosport.registration.application.dto.RegistrationResponse;
import com.accesosport.registration.domain.exception.DuplicateRegistrationException;
import com.accesosport.registration.domain.exception.NoCapacityException;
import com.accesosport.registration.domain.exception.RegistrationNotOpenException;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.model.RegistrationStatus;
import com.accesosport.registration.domain.repository.RegistrationRepository;
import com.accesosport.shared.domain.events.DomainEventPublisher;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterParticipantUseCaseTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private EventModalityRepository eventModalityRepository;
    @Mock private DomainEventPublisher domainEventPublisher;
    @Mock private UserRepository userRepository;
    @Mock private Event event;

    private RegisterParticipantUseCase useCase;
    private UUID eventId;
    private UUID participantId;
    private UUID modalityId;
    private EventModality modality;

    @BeforeEach
    void setUp() {
        useCase = new RegisterParticipantUseCase(
                registrationRepository, eventRepository, domainEventPublisher, eventModalityRepository, userRepository
        );
        when(event.getWaiverTemplate()).thenReturn(null);
        when(userRepository.findById(any())).thenReturn(Optional.of(mock(User.class)));
        eventId = UUID.randomUUID();
        participantId = UUID.randomUUID();
        modalityId = UUID.randomUUID();

        modality = EventModality.reconstitute(
                modalityId, eventId, "21K", new BigDecimal("21.097"),
                DistanceUnit.KM, new BigDecimal("350.00"), null, 200, 50
        );

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(registrationRepository.existsByEventIdAndParticipantId(eventId, participantId)).thenReturn(false);
        when(registrationRepository.save(any(Registration.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventModalityRepository.findByEventId(eventId)).thenReturn(List.of(modality));
        when(eventModalityRepository.reserveIfAvailable(modalityId)).thenReturn(1);
    }

    @Test
    void modalidadDePago_creaRegistroPENDING_PAYMENT() {
        RegistrationResponse result = useCase.execute(new RegisterParticipantCommand(eventId, participantId, modalityId, true, true));

        assertThat(result.status()).isEqualTo(RegistrationStatus.PENDING_PAYMENT.name());
        verify(domainEventPublisher, never()).publish(any());
    }

    @Test
    void modalidadGratuita_creaRegistroCONFIRMED_yPublicaEvento() {
        EventModality freeModality = EventModality.reconstitute(
                modalityId, eventId, "5K Gratis", new BigDecimal("5"),
                DistanceUnit.KM, BigDecimal.ZERO, null, 300, 0
        );
        when(eventModalityRepository.findByEventId(eventId)).thenReturn(List.of(freeModality));

        RegistrationResponse result = useCase.execute(new RegisterParticipantCommand(eventId, participantId, modalityId, true, true));

        assertThat(result.status()).isEqualTo(RegistrationStatus.CONFIRMED.name());
        verify(domainEventPublisher).publish(any());
    }

    @Test
    void cupoDeModalidadAgotado_lanzaNoCapacityException() {
        when(eventModalityRepository.reserveIfAvailable(modalityId)).thenReturn(0);
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);

        assertThatThrownBy(() -> useCase.execute(new RegisterParticipantCommand(eventId, participantId, modalityId, true, true)))
                .isInstanceOf(NoCapacityException.class);

        verify(registrationRepository, never()).save(any());
    }

    @Test
    void sinModalidadSeleccionada_lanzaIllegalArgument() {
        assertThatThrownBy(() -> useCase.execute(new RegisterParticipantCommand(eventId, participantId, null, true, true)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void modalidadDeOtroEvento_lanzaIllegalArgument() {
        UUID otherModalityId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(new RegisterParticipantCommand(eventId, participantId, otherModalityId, true, true)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void duplicado_lanzaDuplicateRegistrationException_antesDeReservar() {
        when(registrationRepository.existsByEventIdAndParticipantId(eventId, participantId)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(new RegisterParticipantCommand(eventId, participantId, modalityId, true, true)))
                .isInstanceOf(DuplicateRegistrationException.class);

        verify(eventModalityRepository, never()).reserveIfAvailable(any());
    }

    @Test
    void eventoNoEncontrado_lanzaRegistrationNotOpenException() {
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RegisterParticipantCommand(eventId, participantId, modalityId, true, true)))
                .isInstanceOf(RegistrationNotOpenException.class);
    }

    @Test
    void guardaModalityIdEnLaRegistration() {
        ArgumentCaptor<Registration> captor = ArgumentCaptor.forClass(Registration.class);

        useCase.execute(new RegisterParticipantCommand(eventId, participantId, modalityId, true, true));

        verify(registrationRepository).save(captor.capture());
        assertThat(captor.getValue().getModalityId()).isEqualTo(modalityId);
    }

    @Test
    void reservaEnModalidad_noEnCapacidadGlobal() {
        useCase.execute(new RegisterParticipantCommand(eventId, participantId, modalityId, true, true));

        verify(eventModalityRepository).reserveIfAvailable(modalityId);
    }
}
