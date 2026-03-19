package com.accesosport.user.application.dto;

import com.accesosport.user.domain.model.UserOrganizerProfile;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the response for an organizer profile, encapsulating details about the organization,
 * its contact, and its current verification status.
 *
 * @param id                 The unique identifier of the organizer profile.
 * @param organizationName   The name of the organization associated with the profile.
 * @param contactName        The name of the primary contact person for the organization.
 * @param phone              The phone number of the contact person or organization.
 * @param address            The address details associated with the organization. This is represented by an AddressDto.
 * @param website            The website URL of the organization, if available.
 * @param facebook           The Facebook profile or page URL for the organization, if available.
 * @param instagram          The Instagram profile URL for the organization, if available.
 * @param description        A brief description of the organization.
 * @param verificationStatus The current verification status of the organizer profile.
 * @param verifiedAt         The timestamp when the profile was successfully verified, if applicable.
 * @param createdAt          The timestamp when the organizer profile was created.
 * @param updatedAt          The timestamp when the organizer profile was last updated.
 */
public record OrganizerProfileResponse(
        UUID id,
        String organizationName,
        String website,
        String facebook,
        String instagram,
        String description,
        String verificationStatus,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /**
     * Converts a UserOrganizerProfile domain object to an OrganizerProfileResponse object.
     *
     * @param domain The UserOrganizerProfile domain object containing the organizer's profile details.
     * @return An OrganizerProfileResponse object representing the converted organizer profile.
     */
    public static OrganizerProfileResponse fromDomain(UserOrganizerProfile domain) {
        return new OrganizerProfileResponse(
                domain.getId(),
                domain.getOrganizationName(),
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
