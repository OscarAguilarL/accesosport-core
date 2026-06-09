package com.accesosport.payment.application.dto;

public record ConnectStatusResponse(
        String stripeAccountId,
        boolean onboardingCompleted,
        boolean chargesEnabled
) {}
