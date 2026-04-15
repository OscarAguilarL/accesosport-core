package com.accesosport.registration.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Registration {

    private UUID id;
    private UUID eventId;
    private UUID participantId;
    private RegistrationStatus status;
    private String ticketCode;
    private Integer bibNumber;
    @Setter
    private PaymentMethod paymentMethod;
    private boolean kitPickedUp;
    private LocalDateTime kitPickedUpAt;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;

    private Registration() {
    }

    public static Registration create(UUID eventId, UUID participantId, RegistrationStatus status) {
        Registration registration = new Registration();
        registration.id = UUID.randomUUID();
        registration.eventId = eventId;
        registration.participantId = participantId;
        registration.status = status;
        registration.ticketCode = TicketCodeGenerator.generate();
        registration.bibNumber = null;
        registration.paymentMethod = null;
        registration.kitPickedUp = false;
        registration.kitPickedUpAt = null;
        registration.registeredAt = LocalDateTime.now();
        registration.cancelledAt = null;
        return registration;
    }

    public static Registration reconstitute(
            UUID id,
            UUID eventId,
            UUID participantId,
            RegistrationStatus status,
            String ticketCode,
            Integer bibNumber,
            PaymentMethod paymentMethod,
            boolean kitPickedUp,
            LocalDateTime kitPickedUpAt,
            LocalDateTime registeredAt,
            LocalDateTime cancelledAt
    ) {
        Registration registration = new Registration();
        registration.id = id;
        registration.eventId = eventId;
        registration.participantId = participantId;
        registration.status = status;
        registration.ticketCode = ticketCode;
        registration.bibNumber = bibNumber;
        registration.paymentMethod = paymentMethod;
        registration.kitPickedUp = kitPickedUp;
        registration.kitPickedUpAt = kitPickedUpAt;
        registration.registeredAt = registeredAt;
        registration.cancelledAt = cancelledAt;
        return registration;
    }

    public void cancel() {
        this.status = RegistrationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void assignBibNumber(int number) {
        if (this.bibNumber != null) {
            throw new IllegalStateException("Bib number already assigned");
        }
        this.bibNumber = number;
    }

    public void markKitPickedUp() {
        if (this.kitPickedUp) {
            return;
        }
        this.kitPickedUp = true;
        this.kitPickedUpAt = LocalDateTime.now();
    }
}
