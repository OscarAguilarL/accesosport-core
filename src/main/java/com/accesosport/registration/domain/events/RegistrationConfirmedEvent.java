package com.accesosport.registration.domain.events;

import com.accesosport.shared.domain.events.DomainEvent;

import java.util.UUID;

/**
 * Se publica cuando un participante confirma su inscripción a un evento.
 * Para eventos gratuitos se publica al registrarse; para eventos de pago, tras confirmar el pago.
 *
 * Constructor con primitivos — MVP-01 añadirá un constructor que acepte la entidad Registration.
 */
public class RegistrationConfirmedEvent extends DomainEvent {

    private final UUID registrationId;
    private final UUID eventId;
    private final UUID participantId;
    private final String ticketCode;
    private final int bibNumber;

    public RegistrationConfirmedEvent(
            UUID registrationId,
            UUID eventId,
            UUID participantId,
            String ticketCode,
            int bibNumber
    ) {
        super("registration.confirmed");
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.participantId = participantId;
        this.ticketCode = ticketCode;
        this.bibNumber = bibNumber;
    }

    public UUID getRegistrationId() {
        return registrationId;
    }

    public UUID getEventId() {
        return eventId;
    }

    public UUID getParticipantId() {
        return participantId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public int getBibNumber() {
        return bibNumber;
    }
}
