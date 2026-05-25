package com.accesosport.registration.domain.events;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationCancelledEventTest {

    @Test
    void eventType_is_registration_cancelled() {
        RegistrationCancelledEvent event = new RegistrationCancelledEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        assertThat(event.getEventType()).isEqualTo("registration.cancelled");
    }

    @Test
    void allFields_areStoredCorrectly() {
        UUID registrationId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        RegistrationCancelledEvent event = new RegistrationCancelledEvent(registrationId, eventId, participantId);

        assertThat(event.getRegistrationId()).isEqualTo(registrationId);
        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getParticipantId()).isEqualTo(participantId);
    }
}
