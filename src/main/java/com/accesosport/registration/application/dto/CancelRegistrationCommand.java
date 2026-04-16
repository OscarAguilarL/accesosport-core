package com.accesosport.registration.application.dto;

import java.util.UUID;

public record CancelRegistrationCommand(UUID registrationId, UUID requesterId, boolean isAdmin) {}
