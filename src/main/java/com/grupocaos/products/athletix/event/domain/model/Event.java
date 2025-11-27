package com.grupocaos.products.athletix.event.domain.model;

import com.grupocaos.products.athletix.event.domain.exception.EventNotPublishableException;
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
            throw new EventNotPublishableException("Only events in DRAFT status can be published. Current status: " + status);
        }

        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new EventNotPublishableException("Cannot publish an event with a past date");
        }
        this.status = EventStatus.PUBLISHED;
        this.updatedOn = LocalDateTime.now();
    }

    public void openRegistration() {
        if (!status.canOpenRegistration()) {
            throw new IllegalStateException("Registration can only be opened in PUBLISHED events. Current status: " + status);
        }
        if (!registrationPeriod.isOpen()) {
            throw new IllegalStateException("Registration period is no longer in effect");
        }
        this.status = EventStatus.REGISTRATION_OPEN;
        this.updatedOn = LocalDateTime.now();
    }

    public void closeRegistration() {
        if (status != EventStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Registration is not open");
        }
        this.status = EventStatus.REGISTRATION_CLOSED;
        this.updatedOn = LocalDateTime.now();
    }

    public void begin() {
        if (status != EventStatus.REGISTRATION_CLOSED) {
            throw new IllegalStateException("The event must have closed registration in order to begin");
        }
        this.status = EventStatus.IN_PROGRESS;
        this.updatedOn = LocalDateTime.now();
    }

    public void complete() {
        if (status != EventStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only events IN PROGRESS can be completed");
        }
        this.status = EventStatus.COMPLETED;
        this.updatedOn = LocalDateTime.now();
    }

    public void cancel() {
        if (!status.canBeCancelled()) {
            throw new IllegalStateException("Cannot cancel an event that is in status " + status);
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
            throw new IllegalStateException("The event is no longer accepting registrations");
        }
        this.registeredParticipants++;
        this.updatedOn = LocalDateTime.now();
    }

    public void decrementRegisteredParticipants() {
        if (registeredParticipants <= 0) {
            throw new IllegalStateException("There are no registered participants");
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
            throw new IllegalArgumentException("The event name is required");
        }
        if (name.length() < 5 || name.length() > 200) {
            throw new IllegalArgumentException("The name must be between 5 and 200 characters");
        }
        if (eventDate == null) {
            throw new IllegalArgumentException("Event date is required");
        }
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event date must be a future date");
        }
        if (location == null) {
            throw new IllegalArgumentException("Location is required");
        }
        if (distance == null) {
            throw new IllegalArgumentException("Distance is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The registration fee must be greater than zero");
        }
        if (registrationPeriod == null) {
            throw new IllegalArgumentException("Registration period is required");
        }
        if (registrationPeriod.end().isAfter(eventDate)) {
            throw new IllegalArgumentException("Registration period must close before event date");
        }
        if (maxParticipants != null && maxParticipants <= 0) {
            throw new IllegalArgumentException("The maximum number of participants must be greater than zero");
        }
        if (createdBy == null) {
            throw new IllegalArgumentException("The organizer is required");
        }
    }
}
