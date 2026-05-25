package com.accesosport.image.domain.usecase;

import com.accesosport.event.domain.exception.EventNotFoundException;
import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.image.domain.exception.InvalidImageException;
import com.accesosport.image.domain.port.ImageStoragePort;
import com.accesosport.image.domain.port.UploadResult;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class UploadEventCoverImageUseCase
        extends UseCase<UploadEventCoverImageUseCase.UploadEventCoverImageCommand, UploadEventCoverImageUseCase.UploadEventCoverImageResult> {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE_BYTES = 5_242_880L;

    private final EventRepository eventRepository;
    private final ImageStoragePort storagePort;

    @Override
    protected UploadEventCoverImageResult internalExecute(UploadEventCoverImageCommand command) {
        if (!ALLOWED_TYPES.contains(command.contentType())) {
            throw new InvalidImageException(MessageKeys.Images.INVALID_IMAGE_TYPE);
        }
        if (command.sizeBytes() > MAX_SIZE_BYTES) {
            throw new InvalidImageException(MessageKeys.Images.INVALID_IMAGE_SIZE);
        }

        Event event = eventRepository.findById(command.eventId())
                .orElseThrow(() -> new EventNotFoundException(command.eventId()));

        if (event.getCoverImagePublicId() != null) {
            storagePort.delete(event.getCoverImagePublicId());
        }

        UploadResult result = storagePort.upload(
                command.bytes(),
                command.contentType(),
                "events/" + command.eventId() + "/cover",
                command.eventId()
        );

        event.setCoverImageUrl(result.url());
        event.setCoverImagePublicId(result.publicId());

        Event saved = eventRepository.save(event);
        return new UploadEventCoverImageResult(saved);
    }

    public record UploadEventCoverImageCommand(UUID eventId, byte[] bytes, String contentType, long sizeBytes) {
    }

    public record UploadEventCoverImageResult(Event event) {
    }
}
