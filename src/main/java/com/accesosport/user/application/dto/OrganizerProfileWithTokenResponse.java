package com.accesosport.user.application.dto;

public record OrganizerProfileWithTokenResponse(
        String token,
        OrganizerProfileResponse profile
) {
}
