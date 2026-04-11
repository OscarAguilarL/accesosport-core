package com.accesosport.event.application.dto;

import com.accesosport.event.domain.model.*;
import com.accesosport.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests for EventResponseMapper, focused on the capacity-related fields
 * that moved from Event to EventCapacity in ARCH-02.
 */
@ExtendWith(MockitoExtension.class)
class EventResponseMapperTest {

    @Mock
    private Event event;

    @Mock
    private User organizer;

    private final UUID eventId = UUID.randomUUID();
    private final Location location = Location.of("Bosque de Chapultepec", "CDMX", "México", null, null);
    private final RegistrationPeriod openPeriod = RegistrationPeriod.of(
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(30)
    );
    private final RegistrationPeriod closedPeriod = RegistrationPeriod.of(
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(1)
    );

    @BeforeEach
    void setUpEventMock() {
        when(organizer.getId()).thenReturn(UUID.randomUUID());
        when(organizer.getEmail()).thenReturn("organizer@test.com");

        when(event.getId()).thenReturn(eventId);
        when(event.getName()).thenReturn("Maratón CDMX");
        when(event.getDescription()).thenReturn("Descripción");
        when(event.getEventDate()).thenReturn(LocalDateTime.now().plusMonths(3));
        when(event.getLocation()).thenReturn(location);
        when(event.getRaceType()).thenReturn(RaceType.MARATHON);
        when(event.getDistance()).thenReturn(Distance.of(BigDecimal.valueOf(42.195), DistanceUnit.KM));
        when(event.getPrice()).thenReturn(BigDecimal.valueOf(500));
        when(event.getCoverImageUrl()).thenReturn(null);
        when(event.getCreatedOn()).thenReturn(LocalDateTime.now());
        when(event.getCreatedBy()).thenReturn(organizer);
    }

    // --- canRegister ---

    @Test
    void canRegister_trueWhenOpenStatusAndOpenPeriodAndCapacityAvailable() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);
        var capacity = EventCapacity.reconstitute(eventId, 5, 100);

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.canRegister()).isTrue();
    }

    @Test
    void canRegister_falseWhenStatusIsNotRegistrationOpen() {
        when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);
        var capacity = EventCapacity.reconstitute(eventId, 0, 100);

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.canRegister()).isFalse();
    }

    @Test
    void canRegister_falseWhenRegistrationPeriodIsClosed() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(closedPeriod);
        var capacity = EventCapacity.reconstitute(eventId, 0, 100);

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.canRegister()).isFalse();
    }

    @Test
    void canRegister_falseWhenCapacityIsFull() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);
        var capacity = EventCapacity.reconstitute(eventId, 100, 100); // full

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.canRegister()).isFalse();
    }

    @Test
    void canRegister_trueWhenUnlimitedCapacity() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);
        var capacity = EventCapacity.reconstitute(eventId, 999, null); // unlimited

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.canRegister()).isTrue();
    }

    // --- Capacity fields come from EventCapacity, not Event ---

    @Test
    void registeredParticipants_comesFromCapacityReserved() {
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);
        var capacity = EventCapacity.reconstitute(eventId, 42, 100);

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.registeredParticipants()).isEqualTo(42);
    }

    @Test
    void registrationsAvailable_comesFromCapacityGetAvailable() {
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);
        var capacity = EventCapacity.reconstitute(eventId, 30, 100);

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.registrationsAvailable()).isEqualTo(70);
    }

    @Test
    void maxParticipants_comesFromCapacityMaxCapacity() {
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);
        var capacity = EventCapacity.reconstitute(eventId, 0, 500);

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.maxParticipants()).isEqualTo(500);
    }

    @Test
    void maxParticipants_isNullWhenUnlimited() {
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);
        var capacity = EventCapacity.create(eventId, null);

        EventResponse response = EventResponseMapper.toEventResponse(event, capacity, List.of());

        assertThat(response.maxParticipants()).isNull();
    }
}
