package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.model.DistanceUnit;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.user.domain.model.RoleEnumeration;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEventUseCaseTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventModalityRepository eventModalityRepository;
    @Mock private User organizer;

    private UUID organizerId;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        when(organizer.hasRole(RoleEnumeration.ROLE_ORGANIZER)).thenReturn(true);
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventModalityRepository.save(any(EventModality.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_savesEventAndModalities() {
        CreateEventUseCase.CreateEventCommand command = buildCommand(List.of(
                new CreateEventUseCase.ModalityData("10K", BigDecimal.TEN, DistanceUnit.KM, new BigDecimal("150"), 300),
                new CreateEventUseCase.ModalityData("21K", new BigDecimal("21.097"), DistanceUnit.KM, new BigDecimal("350"), 200)
        ));

        CreateEventUseCase.CreateEventResult result =
                new CreateEventUseCase(eventRepository, userRepository, eventModalityRepository).execute(command);

        verify(eventRepository).save(any(Event.class));
        verify(eventModalityRepository, times(2)).save(any(EventModality.class));
        assertThat(result.modalities()).hasSize(2);
    }

    @Test
    void execute_modalitiesAreLinkedToSavedEvent() {
        CreateEventUseCase.CreateEventCommand command = buildCommand(List.of(
                new CreateEventUseCase.ModalityData("5K", new BigDecimal("5"), DistanceUnit.KM, BigDecimal.ZERO, 500)
        ));

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        ArgumentCaptor<EventModality> modalityCaptor = ArgumentCaptor.forClass(EventModality.class);

        new CreateEventUseCase(eventRepository, userRepository, eventModalityRepository).execute(command);

        verify(eventRepository).save(eventCaptor.capture());
        verify(eventModalityRepository).save(modalityCaptor.capture());

        assertThat(modalityCaptor.getValue().getEventId())
                .isEqualTo(eventCaptor.getValue().getId());
    }

    private CreateEventUseCase.CreateEventCommand buildCommand(List<CreateEventUseCase.ModalityData> modalities) {
        return new CreateEventUseCase.CreateEventCommand(
                "Maratón CDMX 2027",
                "Carrera de montaña por la ciudad",
                LocalDateTime.now().plusMonths(6),
                "Bosque de Chapultepec",
                "CDMX",
                "México",
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now().plusMonths(3),
                modalities,
                organizerId
        );
    }
}
