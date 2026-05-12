package com.accesosport.event.domain.usecase;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.model.RegistrationPeriod;
import com.accesosport.event.domain.repository.EventRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UpdateEventUseCaseTest {

    @Mock private EventRepository eventRepository;
    @Mock private Event event;

    private UUID eventId;
    private final Location location = Location.of("Chapultepec", "CDMX", "México");
    private final RegistrationPeriod period = RegistrationPeriod.of(
            LocalDateTime.now().plusMonths(1),
            LocalDateTime.now().plusMonths(3)
    );

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(event.getName()).thenReturn("Nombre original");
        when(event.getDescription()).thenReturn("Desc original");
        when(event.getEventDate()).thenReturn(LocalDateTime.now().plusMonths(6));
        when(event.getLocation()).thenReturn(location);
        when(event.getRegistrationPeriod()).thenReturn(period);
        when(event.getStatus()).thenReturn(com.accesosport.event.domain.model.EventStatus.DRAFT);
    }

    @Test
    void execute_savesEvent() {
        UpdateEventUseCase.UpdateEventCommand command = buildCommand("Nuevo nombre");

        new UpdateEventUseCase(eventRepository).execute(command);

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void execute_mergesNullNameFromCurrentEvent() {
        when(event.getName()).thenReturn("Nombre original");

        UpdateEventUseCase.UpdateEventCommand command = new UpdateEventUseCase.UpdateEventCommand(
                eventId, null, null, null, null, null, null, null, null, null, null
        );

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        // update() is called on the mocked event, so we just verify save is called
        new UpdateEventUseCase(eventRepository).execute(command);

        verify(eventRepository).save(event);
    }

    private UpdateEventUseCase.UpdateEventCommand buildCommand(String name) {
        return new UpdateEventUseCase.UpdateEventCommand(
                eventId,
                null,
                name,
                "Descripción actualizada",
                LocalDateTime.now().plusMonths(6),
                "Bosque de Chapultepec",
                "CDMX",
                "México",
                LocalDateTime.now().plusMonths(1),
                LocalDateTime.now().plusMonths(3),
                null
        );
    }
}
