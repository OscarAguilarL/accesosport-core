package com.accesosport.user.application.usecase;

import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.usecase.UseCase;
import com.accesosport.shared.domain.valueobjects.VerificationStatus;
import com.accesosport.user.application.dto.AdminOrganizerListItemResponse;
import com.accesosport.user.domain.exception.OrganizerVerificationPrerequisiteException;
import com.accesosport.user.domain.exception.ProfileNotFoundException;
import com.accesosport.user.domain.model.UserOrganizerProfile;
import com.accesosport.user.domain.repository.OrganizerProfileRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class ApproveOrganizerVerificationUseCase
        extends UseCase<ApproveOrganizerVerificationUseCase.Command, AdminOrganizerListItemResponse> {

    private final OrganizerProfileRepository organizerProfileRepository;

    @Override
    protected AdminOrganizerListItemResponse internalExecute(Command command) {
        UserOrganizerProfile profile = organizerProfileRepository.findById(command.organizerProfileId())
                .orElseThrow(() -> new ProfileNotFoundException(MessageKeys.Users.USER_PROFILE_ORGANIZER_NOT_FOUND));

        AdminOrganizerListItemResponse snapshot = AdminOrganizerListItemResponse.fromDomain(profile);
        if (!snapshot.personalDataComplete()) {
            throw new OrganizerVerificationPrerequisiteException(
                    MessageKeys.Admin.ORGANIZER_PERSONAL_DATA_INCOMPLETE);
        }
        if (!profile.isStripeLinked()) {
            throw new OrganizerVerificationPrerequisiteException(
                    MessageKeys.Admin.ORGANIZER_STRIPE_NOT_LINKED);
        }

        if (profile.getVerificationStatus() == VerificationStatus.NOT_SUBMITTED) {
            profile.submitForReview();
        }
        profile.approveVerification();

        UserOrganizerProfile saved = organizerProfileRepository.save(profile);
        return AdminOrganizerListItemResponse.fromDomain(saved);
    }

    public record Command(UUID organizerProfileId) {
    }
}
