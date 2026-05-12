package com.accesosport.registration.application.dto;

import java.util.UUID;

public record RegisterParticipantRequest(UUID modalityId, boolean waiverAccepted, boolean wantsShirt) {}
