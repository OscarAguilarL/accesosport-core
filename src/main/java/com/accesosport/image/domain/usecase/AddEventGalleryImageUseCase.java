package com.accesosport.image.domain.usecase;

import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.image.domain.exception.InvalidImageException;
import com.accesosport.image.domain.model.EventImage;
import com.accesosport.image.domain.port.ImageStoragePort;
import com.accesosport.image.domain.port.UploadResult;
import com.accesosport.image.domain.repository.EventImageRepository;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class AddEventGalleryImageUseCase
        extends UseCase<AddEventGalleryImageUseCase.AddEventGalleryImageCommand, AddEventGalleryImageUseCase.AddEventGalleryImageResult> {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE_BYTES = 5_242_880L;

    private final EventRepository eventRepository;
    private final EventImageRepository eventImageRepository;
    private final ImageStoragePort storagePort;

    @Override
    protected AddEventGalleryImageResult internalExecute(AddEventGalleryImageCommand command) {
        if (!ALLOWED_TYPES.contains(command.contentType())) {
            throw new InvalidImageException(MessageKeys.Images.INVALID_IMAGE_TYPE);
        }
        if (command.sizeBytes() > MAX_SIZE_BYTES) {
            throw new InvalidImageException(MessageKeys.Images.INVALID_IMAGE_SIZE);
        }

        eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        int order = (int) (eventImageRepository.countByEventId(command.eventId()) + 1);

        UploadResult result = storagePort.upload(
                command.bytes(),
                command.contentType(),
                "events/" + command.eventId() + "/gallery",
                UUID.randomUUID()
        );

        EventImage image = EventImage.create(command.eventId(), result.url(), result.publicId(), order);
        EventImage saved = eventImageRepository.save(image);
        return new AddEventGalleryImageResult(saved);
    }

    public record AddEventGalleryImageCommand(UUID eventId, byte[] bytes, String contentType, long sizeBytes) {
    }

    public record AddEventGalleryImageResult(EventImage image) {
    }
}
