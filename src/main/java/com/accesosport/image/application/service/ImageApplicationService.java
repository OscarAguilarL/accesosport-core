package com.accesosport.image.application.service;

import com.accesosport.event.application.dto.EventResponse;
import com.accesosport.event.application.dto.EventResponseMapper;
import com.accesosport.event.domain.model.EventModality;
import com.accesosport.event.domain.repository.EventModalityRepository;
import com.accesosport.event.domain.repository.EventRepository;
import com.accesosport.image.application.dto.EventImageResponse;
import com.accesosport.image.domain.port.ImageStoragePort;
import com.accesosport.image.domain.repository.EventImageRepository;
import com.accesosport.image.domain.usecase.AddEventGalleryImageUseCase;
import com.accesosport.image.domain.usecase.RemoveEventGalleryImageUseCase;
import com.accesosport.image.domain.usecase.UploadEventCoverImageUseCase;
import com.accesosport.image.domain.usecase.UploadOrganizerLogoUseCase;
import com.accesosport.user.application.dto.OrganizerProfileResponse;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageApplicationService {

    private final EventRepository eventRepository;
    private final EventModalityRepository eventModalityRepository;
    private final EventImageRepository eventImageRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final ImageStoragePort imageStoragePort;

    @Transactional
    public EventResponse uploadEventCoverImage(UUID eventId, byte[] bytes, String contentType, long sizeBytes) {
        UploadEventCoverImageUseCase useCase = new UploadEventCoverImageUseCase(eventRepository, imageStoragePort);
        UploadEventCoverImageUseCase.UploadEventCoverImageResult result = useCase.execute(
                new UploadEventCoverImageUseCase.UploadEventCoverImageCommand(eventId, bytes, contentType, sizeBytes)
        );

        List<EventModality> modalities = eventModalityRepository.findByEventId(eventId);
        List<EventImageResponse> gallery = eventImageRepository.findByEventId(eventId).stream()
                .map(EventImageResponse::fromDomain)
                .toList();

        return EventResponseMapper.toEventResponse(result.event(), modalities, gallery);
    }

    @Transactional
    public EventImageResponse addEventGalleryImage(UUID eventId, byte[] bytes, String contentType, long sizeBytes) {
        AddEventGalleryImageUseCase useCase = new AddEventGalleryImageUseCase(eventRepository, eventImageRepository, imageStoragePort);
        AddEventGalleryImageUseCase.AddEventGalleryImageResult result = useCase.execute(
                new AddEventGalleryImageUseCase.AddEventGalleryImageCommand(eventId, bytes, contentType, sizeBytes)
        );
        return EventImageResponse.fromDomain(result.image());
    }

    @Transactional
    public void removeEventGalleryImage(UUID eventId, UUID imageId) {
        RemoveEventGalleryImageUseCase useCase = new RemoveEventGalleryImageUseCase(eventImageRepository, imageStoragePort);
        useCase.execute(new RemoveEventGalleryImageUseCase.RemoveEventGalleryImageCommand(eventId, imageId));
    }

    @Transactional(readOnly = true)
    public List<EventImageResponse> getEventGallery(UUID eventId) {
        return eventImageRepository.findByEventId(eventId).stream()
                .map(EventImageResponse::fromDomain)
                .toList();
    }

    @Transactional
    public OrganizerProfileResponse uploadOrganizerLogo(UUID userId, byte[] bytes, String contentType, long sizeBytes) {
        UploadOrganizerLogoUseCase useCase = new UploadOrganizerLogoUseCase(organizerProfileRepository, imageStoragePort);
        UploadOrganizerLogoUseCase.UploadOrganizerLogoResult result = useCase.execute(
                new UploadOrganizerLogoUseCase.UploadOrganizerLogoCommand(userId, bytes, contentType, sizeBytes)
        );
        return OrganizerProfileResponse.fromDomain(result.profile());
    }
}
