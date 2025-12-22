package com.grupocaos.products.athletix.user.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents a request to create an organizer profile with essential details.
 *
 * @param organizationName The name of the organization. Must not be blank and can have a maximum length of 120 characters.
 * @param website          The website URL of the organization. Optional, with a maximum length of 200 characters.
 * @param facebook         The Facebook profile or page link for the organization. Optional, with a maximum length of 200 characters.
 * @param instagram        The Instagram profile link for the organization. Optional, with a maximum length of 200 characters.
 * @param description      A brief description of the organization. Optional, with a maximum length of 500 characters.
 */
public record CreateOrganizerProfileRequest(
        @NotBlank
        @Size(max = 120)
        String organizationName,

        @Size(max = 200)
        String website,

        @Size(max = 200)
        String facebook,

        @Size(max = 200)
        String instagram,

        @Size(max = 500)
        String description
) {
}
