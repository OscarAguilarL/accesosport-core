package com.accesosport.image.domain.usecase;

import com.accesosport.image.domain.exception.InvalidImageException;
import com.accesosport.image.domain.port.ImageStoragePort;
import com.accesosport.image.domain.port.UploadResult;
import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.user.domain.exception.ProfileNotFoundException;
import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
public class UploadOrganizerLogoUseCase
        extends UseCase<UploadOrganizerLogoUseCase.UploadOrganizerLogoCommand, UploadOrganizerLogoUseCase.UploadOrganizerLogoResult> {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE_BYTES = 5_242_880L;

    private final OrganizerProfileRepository organizerProfileRepository;
    private final ImageStoragePort storagePort;

    @Override
    protected UploadOrganizerLogoResult internalExecute(UploadOrganizerLogoCommand command) {
        if (!ALLOWED_TYPES.contains(command.contentType())) {
            throw new InvalidImageException(MessageKeys.Images.INVALID_IMAGE_TYPE);
        }
        if (command.sizeBytes() > MAX_SIZE_BYTES) {
            throw new InvalidImageException(MessageKeys.Images.INVALID_IMAGE_SIZE);
        }

        UserOrganizerProfile profile = organizerProfileRepository.findByUserId(command.userId())
                .orElseThrow(() -> new ProfileNotFoundException(MessageKeys.Users.USER_PROFILE_ORGANIZER_NOT_FOUND));

        if (profile.getLogoPublicId() != null) {
            storagePort.delete(profile.getLogoPublicId());
        }

        UploadResult result = storagePort.upload(
                command.bytes(),
                command.contentType(),
                "organizers/" + command.userId() + "/logo",
                command.userId()
        );

        profile.setLogoUrl(result.url());
        profile.setLogoPublicId(result.publicId());

        UserOrganizerProfile saved = organizerProfileRepository.save(profile);
        return new UploadOrganizerLogoResult(saved);
    }

    public record UploadOrganizerLogoCommand(UUID userId, byte[] bytes, String contentType, long sizeBytes) {
    }

    public record UploadOrganizerLogoResult(UserOrganizerProfile profile) {
    }
}
