package com.accesosport.image.infrastructure.persistence.mapper;

import com.accesosport.image.domain.model.EventImage;
import com.accesosport.image.infrastructure.persistence.entity.EventImageJpaEntity;

public class EventImageMapper {

    public static EventImage toDomain(EventImageJpaEntity entity) {
        if (entity == null) return null;

        EventImage image = EventImage.create(
                entity.getEventId(),
                entity.getImageUrl(),
                entity.getImagePublicId(),
                entity.getDisplayOrder()
        );
        image.setId(entity.getId());
        return image;
    }

    public static EventImageJpaEntity toEntity(EventImage domain) {
        if (domain == null) return null;

        EventImageJpaEntity entity = new EventImageJpaEntity();
        entity.setId(domain.getId());
        entity.setEventId(domain.getEventId());
        entity.setImageUrl(domain.getImageUrl());
        entity.setImagePublicId(domain.getImagePublicId());
        entity.setDisplayOrder(domain.getDisplayOrder());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
