package com.accesosport.registration.domain.events;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationConfirmedEventTest {

    @Test
    void eventType_is_registration_confirmed() {
        RegistrationConfirmedEvent event = new RegistrationConfirmedEvent(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "TKT-001", null);

        assertThat(event.getEventType()).isEqualTo("registration.confirmed");
    }

    @Test
    void allFields_areStoredCorrectly() {
        UUID registrationId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();
        String ticketCode = "TKT-2026-0001";
        Integer bibNumber = null; // bibNumber is null at registration time; assigned later

        RegistrationConfirmedEvent event = new RegistrationConfirmedEvent(
                registrationId, eventId, participantId, ticketCode, bibNumber);

        assertThat(event.getRegistrationId()).isEqualTo(registrationId);
        assertThat(event.getEventId()).isEqualTo(eventId);
        assertThat(event.getParticipantId()).isEqualTo(participantId);
        assertThat(event.getTicketCode()).isEqualTo(ticketCode);
        assertThat(event.getBibNumber()).isNull();
    }
}
