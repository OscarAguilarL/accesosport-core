package com.accesosport.event.domain.model;

import com.accesosport.event.domain.exception.EventInvalidStatusException;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.user.domain.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Event {

    @Setter
    private UUID id;
    @Setter
    private int version;
    private String name;
    private String description;
    private LocalDateTime eventDate;
    private Location location;
    private RegistrationPeriod registrationPeriod;
    @Setter
    private EventStatus status;
    private User createdBy;
    @Setter
    private LocalDateTime createdOn;
    @Setter
    private LocalDateTime updatedOn;
    @Setter
    private String coverImageUrl;
    @Setter
    private String coverImagePublicId;
    @Setter
    private LocalDateTime reminderSentAt;

    private Event() {
    }

    public static Event reconstitute(
            UUID id,
            int version,
            String name,
            String description,
            LocalDateTime eventDate,
            Location location,
            RegistrationPeriod registrationPeriod,
            EventStatus status,
            User createdBy,
            LocalDateTime createdOn,
            LocalDateTime updatedOn,
            String coverImageUrl,
            String coverImagePublicId,
            LocalDateTime reminderSentAt
    ) {
        Event event = new Event();
        event.id = id;
        event.version = version;
        event.name = name;
        event.description = description;
        event.eventDate = eventDate;
        event.location = location;
        event.registrationPeriod = registrationPeriod;
        event.status = status;
        event.createdBy = createdBy;
        event.createdOn = createdOn;
        event.updatedOn = updatedOn;
        event.coverImageUrl = coverImageUrl;
        event.coverImagePublicId = coverImagePublicId;
        event.reminderSentAt = reminderSentAt;
        return event;
    }

    public static Event create(
            String name,
            String description,
            LocalDateTime eventDate,
            Location location,
            RegistrationPeriod registrationPeriod,
            User createdBy
    ) {
        Event event = new Event();
        event.id = UUID.randomUUID();
        event.name = name;
        event.description = description;
        event.eventDate = eventDate;
        event.location = location;
        event.registrationPeriod = registrationPeriod;
        event.status = EventStatus.DRAFT;
        event.createdBy = createdBy;
        event.createdOn = LocalDateTime.now();
        event.updatedOn = LocalDateTime.now();

        event.validate();

        return event;
    }

    public void update(String name,
                       String description,
                       LocalDateTime eventDate,
                       Location location,
                       RegistrationPeriod registrationPeriod) {
        if (status != EventStatus.DRAFT) {
            throw new EventInvalidStatusException(MessageKeys.Events.EVENT_UPDATE_ONLY_DRAFT, status);
        }
        this.name = name;
        this.description = description;
        this.eventDate = eventDate;
        this.location = location;
        this.registrationPeriod = registrationPeriod;

        validate();
        this.updatedOn = LocalDateTime.now();
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
            throw new EventInvalidStatusException(MessageKeys.Events.EVENT_CANCEL_INVALID_STATUS, status);
        }
        this.status = EventStatus.CANCELLED;
        this.updatedOn = LocalDateTime.now();
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
        if (registrationPeriod == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_REQUIRED);
        }
        if (registrationPeriod.end().isAfter(eventDate)) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_REGISTRATION_BEFORE_EVENT);
        }
        if (createdBy == null) {
            throw new IllegalArgumentException(MessageKeys.Events.EVENT_VALIDATION_ORGANIZER_REQUIRED);
        }
    }
}
