package com.accesosport.image.domain.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class EventImage {

    @Setter
    private UUID id;
    private UUID eventId;
    private String imageUrl;
    private String imagePublicId;
    private int displayOrder;
    private LocalDateTime createdAt;

    private EventImage() {
    }

    public static EventImage create(UUID eventId, String imageUrl, String imagePublicId, int displayOrder) {
        EventImage image = new EventImage();
        image.id = UUID.randomUUID();
        image.eventId = eventId;
        image.imageUrl = imageUrl;
        image.imagePublicId = imagePublicId;
        image.displayOrder = displayOrder;
        image.createdAt = LocalDateTime.now();
        return image;
    }
}
