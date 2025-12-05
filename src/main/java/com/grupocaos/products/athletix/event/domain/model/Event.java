package com.grupocaos.products.athletix.event.domain.model;

import com.grupocaos.products.athletix.event.domain.exception.EventInvalidStatusException;
import com.grupocaos.products.athletix.shared.i18n.domain.MessageKeys;
import com.grupocaos.products.athletix.user.domain.model.User;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Event {

    @Setter
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime eventDate;
    private Location location;
    private RaceType raceType;
    private Distance distance;
    private BigDecimal price;
    private RegistrationPeriod registrationPeriod;
    private Integer maxParticipants;
    @Setter
    private Integer registeredParticipants;
    @Setter
    private EventStatus status;
    private User createdBy;
    @Setter
    private LocalDateTime createdOn;
    @Setter
    private LocalDateTime updatedOn;

    private Event() {
        this.registeredParticipants = 0;
    }

    public static Event create(
            String name,
            String description,
            LocalDateTime eventDate,
            Location location,
            RaceType raceType,
            Distance distance,
            BigDecimal price,
            RegistrationPeriod registrationPeriod,
            Integer maxParticipants,
            User createdBy
    ) {
        Event event = new Event();
        event.id = UUID.randomUUID();
        event.name = name;
        event.description = description;
        event.eventDate = eventDate;
        event.location = location;
        event.raceType = raceType;
        event.distance = distance;
        event.price = price;
        event.registrationPeriod = registrationPeriod;
        event.maxParticipants = maxParticipants;
        event.status = EventStatus.DRAFT;
        event.createdBy = createdBy;
        event.createdOn = LocalDateTime.now();
        event.updatedOn = LocalDateTime.now();

        event.validate();

        return event;
    }

    public void publish() {
        if (!status.canBePublished()) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_PUBLISH_ONLY_DRAFT);
        }

        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_PUBLISH_PAST_DATE);
        }
        this.status = EventStatus.PUBLISHED;
        this.updatedOn = LocalDateTime.now();
    }

    public void openRegistration() {
        if (!status.canOpenRegistration()) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_REGISTRATION_ONLY_PUBLISHED);
        }
        if (!registrationPeriod.isOpen()) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_REGISTRATION_PERIOD_CLOSED);
        }
        this.status = EventStatus.REGISTRATION_OPEN;
        this.updatedOn = LocalDateTime.now();
    }

    public void closeRegistration() {
        if (status != EventStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_REGISTRATION_NOT_OPEN);
        }
        this.status = EventStatus.REGISTRATION_CLOSED;
        this.updatedOn = LocalDateTime.now();
    }

    public void begin() {
        if (status != EventStatus.REGISTRATION_CLOSED) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_BEGIN_MUST_HAVE_CLOSED_REGISTRATION);
        }
        this.status = EventStatus.IN_PROGRESS;
        this.updatedOn = LocalDateTime.now();
    }

    public void complete() {
        if (status != EventStatus.IN_PROGRESS) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_COMPLETE_ONLY_IN_PROGRESS);
        }
        this.status = EventStatus.COMPLETED;
        this.updatedOn = LocalDateTime.now();
    }

    public void cancel() {
        if (!status.canBeCancelled()) {
            throw new EventInvalidStatusException(MessageKeys.Events.EVENT_CANCEL_INVALID_STATUS, status); // TODO: Cambiar por excepción personalizada para manejar el status + status
        }
        this.status = EventStatus.CANCELLED;
        this.updatedOn = LocalDateTime.now();
    }

    public boolean canRegister() {
        return status.acceptsRegistrations()
                && registrationPeriod.isOpen()
                && (maxParticipants == null || registeredParticipants < maxParticipants);
    }

    public void incrementRegisteredParticipants() {
        if (!canRegister()) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_REGISTRATION_NOT_ACCEPTING);
        }
        this.registeredParticipants++;
        this.updatedOn = LocalDateTime.now();
    }

    public void decrementRegisteredParticipants() {
        if (registeredParticipants <= 0) {
            throw new IllegalStateException(MessageKeys.Events.EVENT_PARTICIPANTS_NONE);
        }
        this.registeredParticipants--;
        this.updatedOn = LocalDateTime.now();
    }

    public boolean isFull() {
        return maxParticipants != null && registeredParticipants >= maxParticipants;
    }

    public int getAvailableParticipants() {
        if (maxParticipants == null) {
            return Integer.MAX_VALUE;
        }
        return Math.max(0, maxParticipants - registeredParticipants);
    }

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_NAME_REQUIRED);
        }
        if (name.length() < 5 || name.length() > 200) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_NAME_LENGTH);
        }
        if (eventDate == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_DATE_REQUIRED);
        }
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_DATE_FUTURE);
        }
        if (location == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_LOCATION_REQUIRED);
        }
        if (distance == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_DISTANCE_REQUIRED);
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_PRICE_POSITIVE);
        }
        if (registrationPeriod == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_REQUIRED);
        }
        if (registrationPeriod.end().isAfter(eventDate)) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_BEFORE_EVENT);
        }
        if (maxParticipants != null && maxParticipants <= 0) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_MAX_PARTICIPANTS_POSITIVE);
        }
        if (createdBy == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_ORGANIZER_REQUIRED);
        }
    }
}
