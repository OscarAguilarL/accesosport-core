package com.accesosport.event.application.dto;

import com.accesosport.event.domain.model.*;
import com.accesosport.user.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventResponseMapperTest {

    @Mock private Event event;
    @Mock private User organizer;

    private final UUID eventId = UUID.randomUUID();
    private final Location location = Location.of("Bosque de Chapultepec", "CDMX", "México");
    private final RegistrationPeriod openPeriod = RegistrationPeriod.of(
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(30)
    );
    private final RegistrationPeriod closedPeriod = RegistrationPeriod.of(
            LocalDateTime.now().minusDays(10),
            LocalDateTime.now().minusDays(1)
    );

    private EventModality modality() {
        return EventModality.reconstitute(UUID.randomUUID(), eventId, "10K",
                new BigDecimal("10"), DistanceUnit.KM, new BigDecimal("150"), null, 5);
    }

    private Optional<EventCapacity> capacityWith(int reserved, int max) {
        return Optional.of(EventCapacity.reconstitute(eventId, reserved, max));
    }

    @BeforeEach
    void setUpEventMock() {
        when(organizer.getId()).thenReturn(UUID.randomUUID());
        when(organizer.getEmail()).thenReturn("organizer@test.com");

        when(event.getId()).thenReturn(eventId);
        when(event.getName()).thenReturn("Maratón CDMX");
        when(event.getDescription()).thenReturn("Descripción");
        when(event.getEventDate()).thenReturn(LocalDateTime.now().plusMonths(3));
        when(event.getLocation()).thenReturn(location);
        when(event.getCoverImageUrl()).thenReturn(null);
        when(event.getCreatedOn()).thenReturn(LocalDateTime.now());
        when(event.getCreatedBy()).thenReturn(organizer);
    }

    @Test
    void canRegister_trueWhenOpenStatusAndOpenPeriodAndCapacityAvailable() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);

        EventResponse response = EventResponseMapper.toEventResponse(
                event, List.of(modality()), capacityWith(5, 100));

        assertThat(response.canRegister()).isTrue();
    }

    @Test
    void canRegister_falseWhenStatusIsNotRegistrationOpen() {
        when(event.getStatus()).thenReturn(EventStatus.PUBLISHED);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);

        EventResponse response = EventResponseMapper.toEventResponse(
                event, List.of(modality()), capacityWith(0, 100));

        assertThat(response.canRegister()).isFalse();
    }

    @Test
    void canRegister_falseWhenRegistrationPeriodIsClosed() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(closedPeriod);

        EventResponse response = EventResponseMapper.toEventResponse(
                event, List.of(modality()), capacityWith(0, 100));

        assertThat(response.canRegister()).isFalse();
    }

    @Test
    void canRegister_falseWhenEventCapacityFull() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);

        EventResponse response = EventResponseMapper.toEventResponse(
                event, List.of(modality()), capacityWith(100, 100));

        assertThat(response.canRegister()).isFalse();
    }

    @Test
    void canRegister_falseWhenNoEventCapacity() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);

        EventResponse response = EventResponseMapper.toEventResponse(
                event, List.of(modality()), Optional.empty());

        assertThat(response.canRegister()).isFalse();
    }

    @Test
    void response_includesAllModalities() {
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);

        List<EventModality> modalities = List.of(modality(), modality());

        EventResponse response = EventResponseMapper.toEventResponse(event, modalities, capacityWith(10, 200));

        assertThat(response.modalities()).hasSize(2);
    }

    @Test
    void response_includesCapacityFields() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);

        EventResponse response = EventResponseMapper.toEventResponse(
                event, List.of(modality()), capacityWith(30, 500));

        assertThat(response.maxCapacity()).isEqualTo(500);
        assertThat(response.registeredCount()).isEqualTo(30);
        assertThat(response.availableSpots()).isEqualTo(470);
    }

    @Test
    void summaryResponse_computesMinPriceAndAvailableSpots() {
        when(event.getStatus()).thenReturn(EventStatus.REGISTRATION_OPEN);
        when(event.getRegistrationPeriod()).thenReturn(openPeriod);

        EventModality cheap = EventModality.reconstitute(UUID.randomUUID(), eventId, "5K",
                new BigDecimal("5"), DistanceUnit.KM, new BigDecimal("100"), null, 50);
        EventModality expensive = EventModality.reconstitute(UUID.randomUUID(), eventId, "21K",
                new BigDecimal("21.097"), DistanceUnit.KM, new BigDecimal("350"), null, 30);

        EventSummaryResponse summary = EventResponseMapper.toEventSummaryResponse(
                event, List.of(cheap, expensive), capacityWith(80, 500));

        assertThat(summary.minPrice()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(summary.totalAvailableSpots()).isEqualTo(420);
    }
}
