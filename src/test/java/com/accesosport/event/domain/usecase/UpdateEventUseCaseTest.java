package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.model.DistanceUnit;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.EventCapacity;
import com.accesosport.event.domain.model.RaceType;
import com.accesosport.event.domain.repository.EventCapacityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the EventCapacity update logic introduced in ARCH-02.
 * Verifies that EventCapacity is saved only when maxParticipants changes.
 */
@ExtendWith(MockitoExtension.class)
class UpdateEventUseCaseTest {

    @Mock private EventRepository eventRepository;
    @Mock private EventCapacityRepository eventCapacityRepository;
    @Mock private Event event;
    @Mock private EventCapacity capacity;

    private UUID eventId;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventCapacityRepository.findByEventId(eventId)).thenReturn(Optional.of(capacity));
        when(eventRepository.save(event)).thenReturn(event);
    }

    @Test
    void execute_savesCapacity_whenMaxParticipantsChanges() {
        when(capacity.getMaxCapacity()).thenReturn(100);

        UpdateEventUseCase.UpdateEventCommand command = buildCommand(200); // changed: 100 → 200

        new UpdateEventUseCase(eventRepository, eventCapacityRepository).execute(command);

        verify(capacity).updateMaxCapacity(200);
        verify(eventCapacityRepository).save(capacity);
    }

    @Test
    void execute_doesNotSaveCapacity_whenMaxParticipantsIsExplicitlySameValue() {
        when(capacity.getMaxCapacity()).thenReturn(100);

        UpdateEventUseCase.UpdateEventCommand command = buildCommand(100); // same value

        new UpdateEventUseCase(eventRepository, eventCapacityRepository).execute(command);

        verify(eventCapacityRepository, never()).save(any());
    }

    @Test
    void execute_doesNotSaveCapacity_whenMaxParticipantsIsAbsentInCommand() {
        when(capacity.getMaxCapacity()).thenReturn(100);

        UpdateEventUseCase.UpdateEventCommand command = buildCommand(null); // falls back to capacity

        new UpdateEventUseCase(eventRepository, eventCapacityRepository).execute(command);

        // mergedMaxParticipants = capacity.getMaxCapacity() = 100 = same → no save
        verify(eventCapacityRepository, never()).save(any());
    }

    /**
     * All command fields are non-null (except requesterId and maxParticipants) so the
     * merge logic reads from the command directly and never calls event getters for those fields.
     * requesterId = null skips the ownership check.
     */
    private UpdateEventUseCase.UpdateEventCommand buildCommand(Integer maxParticipants) {
        return new UpdateEventUseCase.UpdateEventCommand(
                eventId,
                null,    // requesterId — skip ownership check
                "Maratón CDMX 2027",
                "Descripción actualizada",
                LocalDateTime.now().plusMonths(6),
                "Bosque de Chapultepec",
                "CDMX",
                "México",
                19.4326,   // latitude — non-null avoids fallback to mocked event.getLocation()
                -99.1332,  // longitude
                RaceType.MARATHON,
                BigDecimal.valueOf(42.195),
                DistanceUnit.KM,
                BigDecimal.valueOf(500),
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now().plusMonths(3),
                maxParticipants
        );
    }
}
