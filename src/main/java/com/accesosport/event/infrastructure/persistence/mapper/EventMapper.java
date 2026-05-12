package com.accesosport.event.infrastructure.persistence.mapper;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.model.RegistrationPeriod;
import com.accesosport.event.infrastructure.persistence.entity.EventJpaEntity;
import com.accesosport.event.infrastructure.persistence.entity.LocationEmbeddable;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.infrastructure.persistence.mapper.UserMapper;

import java.math.BigDecimal;

public class EventMapper {

    public static Event toDomain(EventJpaEntity entity) {
        if (entity == null) return null;

        Location location = mapLocation(entity.getLocation());
        RegistrationPeriod registrationPeriod = RegistrationPeriod.of(
                entity.getRegistrationStart(),
                entity.getRegistrationEnd()
        );
        User organizer = UserMapper.toDomain(entity.getCreatedBy());

        return Event.reconstitute(
                entity.getId(),
                entity.getVersion(),
                entity.getName(),
                entity.getDescription(),
                entity.getEventDate(),
                location,
                registrationPeriod,
                entity.getStatus(),
                organizer,
                entity.getCreatedOn(),
                entity.getUpdatedOn(),
                entity.getCoverImageUrl(),
                entity.getCoverImagePublicId(),
                entity.getReminderSentAt(),
                entity.getWaiverTemplate()
        );
    }

    public static EventJpaEntity toEntity(Event domain) {
        if (domain == null) return null;

        EventJpaEntity entity = new EventJpaEntity();
        entity.setId(domain.getId());
        entity.setVersion(domain.getVersion());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setEventDate(domain.getEventDate());
        entity.setLocation(mapLocationEmbeddable(domain.getLocation()));
        entity.setRegistrationStart(domain.getRegistrationPeriod().start());
        entity.setRegistrationEnd(domain.getRegistrationPeriod().end());
        entity.setStatus(domain.getStatus());
        entity.setCreatedOn(domain.getCreatedOn());
        entity.setUpdatedOn(domain.getUpdatedOn());
        entity.setCoverImageUrl(domain.getCoverImageUrl());
        entity.setCoverImagePublicId(domain.getCoverImagePublicId());
        entity.setReminderSentAt(domain.getReminderSentAt());
        entity.setWaiverTemplate(domain.getWaiverTemplate());
        entity.setCreatedBy(UserMapper.toEntity(domain.getCreatedBy()));

        return entity;
    }

    private static Location mapLocation(LocationEmbeddable loc) {
        if (loc == null) return null;
        return Location.of(
                loc.getPlace(),
                loc.getCity(),
                loc.getCountry()
        );
    }

    private static LocationEmbeddable mapLocationEmbeddable(Location location) {
        if (location == null) return null;
        return new LocationEmbeddable(
                location.place(),
                location.city(),
                location.country()
        );
    }
}
