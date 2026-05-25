package com.accesosport.image.domain.usecase;

import com.accesosport.image.domain.exception.EventImageNotFoundException;
import com.accesosport.image.domain.model.EventImage;
import com.accesosport.image.domain.port.ImageStoragePort;
import com.accesosport.image.domain.repository.EventImageRepository;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class RemoveEventGalleryImageUseCase
        extends UseCase<RemoveEventGalleryImageUseCase.RemoveEventGalleryImageCommand, Void> {

    private final EventImageRepository eventImageRepository;
    private final ImageStoragePort storagePort;

    @Override
    protected Void internalExecute(RemoveEventGalleryImageCommand command) {
        EventImage image = eventImageRepository.findById(command.imageId())
                .orElseThrow(() -> new EventImageNotFoundException(command.imageId()));

        if (!image.getEventId().equals(command.eventId())) {
            throw new EventImageNotFoundException(command.imageId());
        }

        storagePort.delete(image.getImagePublicId());
        eventImageRepository.delete(image);
        return null;
    }

    public record RemoveEventGalleryImageCommand(UUID eventId, UUID imageId) {
    }
}
