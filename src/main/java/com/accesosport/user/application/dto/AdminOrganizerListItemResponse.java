package com.accesosport.user.application.dto;

import com.accesosport.shared.domain.valueobjects.Address;
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.UserOrganizerProfile;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminOrganizerListItemResponse(
        UUID organizerProfileId,
        UUID userId,
        String email,
        String organizationName,
        String logoUrl,
        String verificationStatus,
        LocalDateTime verifiedAt,
        boolean personalDataComplete,
        boolean stripeLinked,
        boolean stripeOnboardingCompleted,
        boolean stripeTransfersActive,
        LocalDateTime createdAt
) {

    public static AdminOrganizerListItemResponse fromDomain(UserOrganizerProfile profile) {
        PersonalData pd = profile.getUser().getPersonalData();
        Address addr = profile.getUser().getAddress();
        boolean personalDataComplete = pd != null
                && pd.getFirstName() != null
                && pd.getLastName() != null
                && pd.getBirthDate() != null
                && pd.getGender() != null
                && pd.getPhoneNumber() != null
                && addr != null;

        return new AdminOrganizerListItemResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getOrganizationName(),
                profile.getLogoUrl(),
                profile.getVerificationStatus().name(),
                profile.getVerifiedAt(),
                personalDataComplete,
                profile.isStripeLinked(),
                profile.isStripeOnboardingCompleted(),
                profile.isStripeTransfersActive(),
                profile.getCreatedAt()
        );
    }
}
