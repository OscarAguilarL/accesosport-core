package com.accesosport.user.domain.model;

import com.accesosport.shared.domain.i18n.MessageKeys;
import com.accesosport.shared.domain.valueobjects.VerificationStatus;
import com.accesosport.user.domain.exception.InvalidVerificationStatusTransitionException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents the profile of an organizer associated with a user in the system.
 * This class encapsulates information about the organization, contact details,
 * verification status, and public presence of the organizer.
 * <p>
 * Key features of this class include:
 * - Managing a verification process through state transitions.
 * - Supporting public-facing attributes like website, Facebook, and Instagram.
 * - Tracking timestamps for creation and updates.
 * <p>
 * Instances of this class are typically used to store and manage organizer-specific
 * details, along with their associated account and verification status.
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserOrganizerProfile {

    private UUID id;

    private String organizationName;

    // TODO: datos fiscales
    // TODO: LOGO URL

    // Public information
    private String website;
    private String facebook;
    private String instagram;
    private String description;

    // Verification
    private VerificationStatus verificationStatus;
    private LocalDateTime verifiedAt;

    private User user;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Creates a new instance of {@code UserOrganizerProfile} with the provided details.
     * This method initializes a profile with mandatory fields and optionally includes
     * website, Facebook, and Instagram information if provided.
     *
     * @param organizationName The name of the organization associated with the profile.
     * @param website          The website URL of the organization, if available.
     * @param facebook         The Facebook profile or page URL of the organization, if available.
     * @param instagram        The Instagram profile or page URL of the organization, if available.
     * @param description      A brief description of the organization.
     * @param user             The {@code User} entity associated with this profile.
     * @return A fully constructed {@code UserOrganizerProfile} instance.
     */
    public static UserOrganizerProfile create(
            String organizationName,
            String website,
            String facebook,
            String instagram,
            String description,
            User user
    ) {
        UserOrganizerProfileBuilder profileBuilder = UserOrganizerProfile.builder()
                .id(UUID.randomUUID())
                .organizationName(organizationName)
                .description(description)
                .verificationStatus(VerificationStatus.NOT_SUBMITTED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user);

        if (website != null) {
            profileBuilder.website(website);

        }

        if (facebook != null) {
            profileBuilder.facebook(facebook);
        }

        if (instagram != null) {
            profileBuilder.instagram(instagram);
        }

        return profileBuilder.build();
    }

    /**
     * Changes the verification status of the UserOrganizerProfile.
     * Updates the status if the transition is valid and modifies the timestamps accordingly.
     * Throws an exception if the transition to the specified status is not allowed.
     *
     * @param nextStatus The new verification status to transition to.
     *                   Must be a valid status transition as per the current verification state.
     * @throws IllegalStateException If the transition to the specified status is not permitted.
     */
    public void changeVerificationStatus(VerificationStatus nextStatus) {
        if (!this.verificationStatus.canTransitionTo(nextStatus)) {
            throw new InvalidVerificationStatusTransitionException(
                    MessageKeys.Users.USER_PROFILE_ORGANIZER_VERIFICATION_INVALID_STATUS,
                    this.verificationStatus.name(),
                    nextStatus.name()
            );
        }

        this.verificationStatus = nextStatus;
        this.updatedAt = LocalDateTime.now();

        if (nextStatus == VerificationStatus.VERIFIED) {
            this.verifiedAt = LocalDateTime.now();
        }
    }

    /**
     * Submits the organizer profile for review by changing its verification status to PENDING_REVIEW.
     * This method triggers a state transition in the verification process, designating the profile
     * as awaiting review.
     * <p>
     * This method depends on the underlying `changeVerificationStatus` functionality to ensure that
     * the status transition to PENDING_REVIEW is valid and handled appropriately.
     *
     * @throws IllegalStateException If transitioning to PENDING_REVIEW is not allowed
     *                               based on the current verification status.
     */
    public void submitForReview() {
        changeVerificationStatus(VerificationStatus.PENDING_REVIEW);
    }

    /**
     * Approves the verification of the UserOrganizerProfile by changing its verification status
     * to VERIFIED. This action marks the profile as successfully verified.
     * <p>
     * Uses the {@code changeVerificationStatus} method to enforce the state transition,
     * ensuring that the operation follows the predefined rules for permissible status changes.
     *
     * @throws IllegalStateException if transitioning to VERIFIED is not allowed
     *                               based on the current verification status.
     */
    public void approveVerification() {
        changeVerificationStatus(VerificationStatus.VERIFIED);
    }

    /**
     * Rejects the verification of the UserOrganizerProfile by changing its
     * verification status to REJECTED. This action indicates that the profile
     * has not passed the verification process.
     * <p>
     * Uses the {@code changeVerificationStatus} method to perform the state
     * transition, ensuring that the transition to REJECTED is permissible based
     * on the current verification status.
     *
     * @throws IllegalStateException if transitioning to REJECTED is not allowed
     *                               based on the current verification status.
     */
    public void rejectVerification() {
        changeVerificationStatus(VerificationStatus.REJECTED);
    }

    /**
     * Determines if the user organizer profile has been verified.
     * <p>
     * This method checks whether the verification status of the profile is set to
     * {@code VerificationStatus.VERIFIED}, indicating that the profile has successfully
     * completed the verification process.
     *
     * @return {@code true} if the profile's verification status is {@code VERIFIED},
     * otherwise {@code false}.
     */
    public boolean isVerified() {
        return this.verificationStatus == VerificationStatus.VERIFIED;
    }
}
