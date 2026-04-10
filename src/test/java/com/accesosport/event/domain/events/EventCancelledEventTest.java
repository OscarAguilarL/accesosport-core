package com.accesosport.event.domain.events;

import com.accesosport.event.domain.model.Event;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventCancelledEventTest {

    @Mock
    private Event event;

    @Test
    void eventType_is_event_cancelled() {
        UUID eventId = UUID.randomUUID();
        when(event.getId()).thenReturn(eventId);
        when(event.getName()).thenReturn("Maratón CDMX");

        EventCancelledEvent domainEvent = new EventCancelledEvent(event, "Lluvia extrema", List.of());

        assertThat(domainEvent.getEventType()).isEqualTo("event.cancelled");
    }

    @Test
    void fields_areMappedFromEvent() {
        UUID eventId = UUID.randomUUID();
        when(event.getId()).thenReturn(eventId);
        when(event.getName()).thenReturn("Maratón CDMX");

        EventCancelledEvent domainEvent = new EventCancelledEvent(event, "Lluvia extrema", List.of());

        assertThat(domainEvent.getEventId()).isEqualTo(eventId);
        assertThat(domainEvent.getEventName()).isEqualTo("Maratón CDMX");
        assertThat(domainEvent.getCancellationReason()).isEqualTo("Lluvia extrema");
    }

    @Test
    void affectedRegistrationIds_areStoredCorrectly() {
        UUID reg1 = UUID.randomUUID();
        UUID reg2 = UUID.randomUUID();
        when(event.getId()).thenReturn(UUID.randomUUID());
        when(event.getName()).thenReturn("Maratón CDMX");

        EventCancelledEvent domainEvent = new EventCancelledEvent(event, "Razón", List.of(reg1, reg2));

        assertThat(domainEvent.getAffectedRegistrationIds()).containsExactly(reg1, reg2);
    }

    @Test
    void affectedRegistrationIds_isImmutable() {
        when(event.getId()).thenReturn(UUID.randomUUID());
        when(event.getName()).thenReturn("Maratón CDMX");
        List<UUID> mutableList = new ArrayList<>();
        mutableList.add(UUID.randomUUID());

        EventCancelledEvent domainEvent = new EventCancelledEvent(event, "Razón", mutableList);

        assertThatThrownBy(() -> domainEvent.getAffectedRegistrationIds().add(UUID.randomUUID()))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
