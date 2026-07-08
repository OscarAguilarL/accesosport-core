package com.accesosport.user.application.usecase;

import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.shared.domain.valueobjects.VerificationStatus;
import com.accesosport.user.application.dto.AdminOrganizerListItemResponse;
import com.accesosport.user.domain.exception.InvalidVerificationStatusTransitionException;
import com.accesosport.user.domain.exception.ProfileNotFoundException;
import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class SubmitOrganizerForReviewUseCase
        extends UseCase<SubmitOrganizerForReviewUseCase.Command, AdminOrganizerListItemResponse> {

    private final OrganizerProfileRepository organizerProfileRepository;

    @Override
    protected AdminOrganizerListItemResponse internalExecute(Command command) {
        UserOrganizerProfile profile = organizerProfileRepository.findById(command.organizerProfileId())
                .orElseThrow(() -> new ProfileNotFoundException(MessageKeys.Users.USER_PROFILE_ORGANIZER_NOT_FOUND));

        if (profile.getVerificationStatus() != VerificationStatus.REJECTED) {
            throw new InvalidVerificationStatusTransitionException(
                    MessageKeys.Users.USER_PROFILE_ORGANIZER_VERIFICATION_INVALID_STATUS,
                    profile.getVerificationStatus().name(),
                    VerificationStatus.PENDING_REVIEW.name()
            );
        }

        profile.submitForReview();
        UserOrganizerProfile saved = organizerProfileRepository.save(profile);
        return AdminOrganizerListItemResponse.fromDomain(saved);
    }

    public record Command(UUID organizerProfileId) {
    }
}
