package com.accesosport.image.application.dto;

import com.accesosport.image.domain.model.EventImage;

import java.util.UUID;

public record EventImageResponse(
        UUID id,
        String imageUrl,
        int displayOrder
) {
    public static EventImageResponse fromDomain(EventImage image) {
        return new EventImageResponse(image.getId(), image.getImageUrl(), image.getDisplayOrder());
    }
}
