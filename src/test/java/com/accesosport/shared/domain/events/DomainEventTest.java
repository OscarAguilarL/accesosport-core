package com.accesosport.shared.domain.events;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventTest {

    @Test
    void eventType_isStoredCorrectly() {
        DomainEvent event = new DomainEvent("test.happened") {};

        assertThat(event.getEventType()).isEqualTo("test.happened");
    }

    @Test
    void eventId_isNotNull() {
        DomainEvent event = new DomainEvent("test.happened") {};

        assertThat(event.getEventId()).isNotNull();
    }

    @Test
    void occurredAt_isSetToNow() {
        Instant before = Instant.now();
        DomainEvent event = new DomainEvent("test.happened") {};
        Instant after = Instant.now();

        assertThat(event.getOccurredAt())
                .isAfterOrEqualTo(before)
                .isBeforeOrEqualTo(after);
    }

    @Test
    void twoInstances_haveDifferentEventIds() {
        DomainEvent first = new DomainEvent("test.happened") {};
        DomainEvent second = new DomainEvent("test.happened") {};

        assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
    }
}
