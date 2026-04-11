package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.model.DistanceUnit;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCapacity;
import com.accesosport.event.domain.model.RaceType;
import com.accesosport.event.domain.repository.EventCapacityRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateEventUseCaseTest {

    @Mock private EventRepository eventRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventCapacityRepository eventCapacityRepository;
    @Mock private User organizer;

    private UUID organizerId;

    @BeforeEach
    void setUp() {
        organizerId = UUID.randomUUID();
        when(organizer.hasRole(RoleEnumeration.ROLE_ORGANIZER)).thenReturn(true);
        when(userRepository.findById(organizerId)).thenReturn(Optional.of(organizer));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void execute_createsEventCapacity_withCorrectEventIdAndMaxParticipants() {
        CreateEventUseCase.CreateEventCommand command = buildCommand(100);

        new CreateEventUseCase(eventRepository, userRepository, eventCapacityRepository).execute(command);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(eventCaptor.capture());
        UUID savedEventId = eventCaptor.getValue().getId();

        ArgumentCaptor<EventCapacity> capacityCaptor = ArgumentCaptor.forClass(EventCapacity.class);
        verify(eventCapacityRepository).save(capacityCaptor.capture());
        EventCapacity savedCapacity = capacityCaptor.getValue();

        assertThat(savedCapacity.getEventId()).isEqualTo(savedEventId);
        assertThat(savedCapacity.getMaxCapacity()).isEqualTo(100);
        assertThat(savedCapacity.getReserved()).isZero();
    }

    @Test
    void execute_createsEventCapacity_withNullMaxCapacity_whenCommandHasNoLimit() {
        CreateEventUseCase.CreateEventCommand command = buildCommand(null);

        new CreateEventUseCase(eventRepository, userRepository, eventCapacityRepository).execute(command);

        ArgumentCaptor<EventCapacity> capacityCaptor = ArgumentCaptor.forClass(EventCapacity.class);
        verify(eventCapacityRepository).save(capacityCaptor.capture());

        assertThat(capacityCaptor.getValue().getMaxCapacity()).isNull();
        assertThat(capacityCaptor.getValue().getReserved()).isZero();
    }

    private CreateEventUseCase.CreateEventCommand buildCommand(Integer maxParticipants) {
        return new CreateEventUseCase.CreateEventCommand(
                "Maratón CDMX 2027",
                "Carrera de montaña por la ciudad",
                LocalDateTime.now().plusMonths(6),
                "Bosque de Chapultepec",
                "CDMX",
                "México",
                null,
                null,
                RaceType.MARATHON,
                BigDecimal.valueOf(42.195),
                DistanceUnit.KM,
                BigDecimal.valueOf(500),
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now().plusMonths(3),
                maxParticipants,
                organizerId
        );
    }
}
