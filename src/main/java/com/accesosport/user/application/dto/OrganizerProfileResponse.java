package com.accesosport.user.application.dto;

import com.accesosport.user.domain.model.UserOrganizerProfile;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizerProfileResponse(
        UUID id,
        String organizationName,
        String logoUrl,
        String website,
        String facebook,
        String instagram,
        String description,
        String verificationStatus,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static OrganizerProfileResponse fromDomain(UserOrganizerProfile domain) {
        return new OrganizerProfileResponse(
                domain.getId(),
                domain.getOrganizationName(),
                domain.getLogoUrl(),
                domain.getWebsite(),
                domain.getFacebook(),
                domain.getInstagram(),
                domain.getDescription(),
                domain.getVerificationStatus().name(),
                domain.getVerifiedAt(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
