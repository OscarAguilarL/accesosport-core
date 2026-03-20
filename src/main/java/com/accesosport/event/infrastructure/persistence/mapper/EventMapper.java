package com.accesosport.event.infrastructure.persistence.mapper;

import com.accesosport.event.domain.model.Distance;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.event.domain.model.RegistrationPeriod;
import com.accesosport.event.infrastructure.persistence.entity.EventJpaEntity;
import com.accesosport.event.infrastructure.persistence.entity.LocationEmbeddable;
import com.accesosport.user.domain.model.User;
import com.accesosport.user.infrastructure.persistence.mapper.UserMapper;

import java.math.BigDecimal;

public class EventMapper {

    public static Event toDomain(EventJpaEntity eventJpaEntity) {
        if (eventJpaEntity == null) return null;

        Location location = mapLocation(eventJpaEntity.getLocation());
        Distance distance = Distance.of(eventJpaEntity.getDistance(), eventJpaEntity.getDistanceUnit());
        RegistrationPeriod registrationPeriod = RegistrationPeriod.of(
                eventJpaEntity.getRegistrationStart(),
                eventJpaEntity.getRegistrationEnd()
        );

        User organizer = UserMapper.toDomain(eventJpaEntity.getCreatedBy());

        Event event = Event.create(
                eventJpaEntity.getName(),
                eventJpaEntity.getDescription(),
                eventJpaEntity.getEventDate(),
                location,
                eventJpaEntity.getRaceType(),
                distance,
                eventJpaEntity.getPrice(),
                registrationPeriod,
                eventJpaEntity.getMaxParticipants(),
                organizer
        );

        event.setId(eventJpaEntity.getId());
        event.setRegisteredParticipants(eventJpaEntity.getRegisteredParticipants());
        event.setStatus(eventJpaEntity.getStatus());
        event.setCreatedOn(eventJpaEntity.getCreatedOn());
        event.setUpdatedOn(eventJpaEntity.getUpdatedOn());
        event.setCoverImageUrl(eventJpaEntity.getCoverImageUrl());
        event.setCoverImagePublicId(eventJpaEntity.getCoverImagePublicId());

        return event;
    }

    public static EventJpaEntity toEntity(Event domain) {
        if (domain == null) return null;

        EventJpaEntity entity = new EventJpaEntity();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setDescription(domain.getDescription());
        entity.setEventDate(domain.getEventDate());
        entity.setLocation(mapLocationEmbeddable(domain.getLocation()));
        entity.setRaceType(domain.getRaceType());
        entity.setDistance(domain.getDistance().value());
        entity.setDistanceUnit(domain.getDistance().unit());
        entity.setPrice(domain.getPrice());
        entity.setRegistrationStart(domain.getRegistrationPeriod().start());
        entity.setRegistrationEnd(domain.getRegistrationPeriod().end());
        entity.setMaxParticipants(domain.getMaxParticipants());
        entity.setRegisteredParticipants(domain.getRegisteredParticipants());
        entity.setStatus(domain.getStatus());
        entity.setCreatedOn(domain.getCreatedOn());
        entity.setUpdatedOn(domain.getUpdatedOn());
        entity.setCoverImageUrl(domain.getCoverImageUrl());
        entity.setCoverImagePublicId(domain.getCoverImagePublicId());
        entity.setCreatedBy(UserMapper.toEntity(domain.getCreatedBy()));

        return entity;
    }

    private static Location mapLocation(LocationEmbeddable locationEmbeddable) {
        if (locationEmbeddable == null) return null;
        return Location.of(
                locationEmbeddable.getPlace(),
                locationEmbeddable.getCity(),
                locationEmbeddable.getCountry(),
                locationEmbeddable.getLatitude().doubleValue(),
                locationEmbeddable.getLongitude().doubleValue()
        );
    }

    private static LocationEmbeddable mapLocationEmbeddable(Location location) {
        if (location == null) return null;

        return new LocationEmbeddable(
                location.place(),
                location.city(),
                location.country(),
                location.latitude() != null ? BigDecimal.valueOf(location.latitude()) : null,
                location.longitude() != null ? BigDecimal.valueOf(location.longitude()) : null
        );
    }
}
