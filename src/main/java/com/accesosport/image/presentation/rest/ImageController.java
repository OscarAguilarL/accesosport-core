package com.accesosport.image.presentation.rest;

import com.accesosport.auth.infrastructure.security.CustomUserDetails;
import com.accesosport.event.application.dto.EventResponse;
import com.accesosport.image.application.dto.EventImageResponse;
import com.accesosport.image.application.service.ImageApplicationService;
import com.accesosport.user.application.dto.OrganizerProfileResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageApplicationService imageApplicationService;

    @PutMapping("/api/v1/events/{eventId}/cover-image")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<EventResponse> uploadCoverImage(
            @PathVariable UUID eventId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        EventResponse response = imageApplicationService.uploadEventCoverImage(
                eventId,
                file.getBytes(),
                file.getContentType(),
                file.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/v1/events/{eventId}/images")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<EventImageResponse> addGalleryImage(
            @PathVariable UUID eventId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        EventImageResponse response = imageApplicationService.addEventGalleryImage(
                eventId,
                file.getBytes(),
                file.getContentType(),
                file.getSize()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/v1/events/{eventId}/images/{imageId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ORGANIZER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> removeGalleryImage(
            @PathVariable UUID eventId,
            @PathVariable UUID imageId
    ) {
        imageApplicationService.removeEventGalleryImage(eventId, imageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/events/{eventId}/images")
    public ResponseEntity<List<EventImageResponse>> getEventGallery(@PathVariable UUID eventId) {
        return ResponseEntity.ok(imageApplicationService.getEventGallery(eventId));
    }

    @PutMapping("/api/v1/user/profile/organizer/logo")
    public ResponseEntity<OrganizerProfileResponse> uploadOrganizerLogo(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) throws IOException {
        OrganizerProfileResponse response = imageApplicationService.uploadOrganizerLogo(
                userDetails.getUserId(),
                file.getBytes(),
                file.getContentType(),
                file.getSize()
        );
        return ResponseEntity.ok(response);
    }
}
