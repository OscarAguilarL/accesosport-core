package com.accesosport.user.application.dto;

public record ParticipantProfileWithTokenResponse(
        String token,
        ParticipantProfileResponse profile
) {
}
