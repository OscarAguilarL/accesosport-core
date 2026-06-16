package com.accesosport.payment.domain.port;

import java.util.UUID;

public interface PaymentProcessorPort {

    record CreateCheckoutSessionCommand(
            UUID registrationId,
            UUID paymentId,
            UUID eventId,
            String eventName,
            long amountTotalCentavos,
            long serviceFeeCentavos,
            String stripeAccountId,
            String idempotencyKey
    ) {}

    record CheckoutSessionResult(String sessionId, String checkoutUrl) {}

    record CheckoutSessionInfo(String url, String status, String paymentIntentId) {}

    record RefundResult(String refundId) {}

    record ConnectAccountResult(String stripeAccountId) {}

    record OnboardingLinkResult(String url) {}

    record ConnectAccountStatus(
            boolean onboardingSubmitted,
            boolean transfersActive,
            boolean payoutsActive,
            boolean hasUserRequirements,
            boolean underStripeReview
    ) {}

    record StripeWebhookEvent(
            String eventId,
            String type,
            String sessionId,
            String paymentIntentId,
            String paymentMethodType,
            String paymentStatus,
            String connectedAccountId
    ) {}

    CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand cmd);

    CheckoutSessionInfo retrieveCheckoutSession(String sessionId);

    RefundResult refund(String paymentIntentId, UUID paymentId);

    String getActualPaymentMethod(String paymentIntentId);

    ConnectAccountResult createConnectAccount(String email, String orgName);

    OnboardingLinkResult createOnboardingLink(String stripeAccountId);

    ConnectAccountStatus getConnectAccountStatus(String stripeAccountId);

    StripeWebhookEvent parseAndValidateCheckoutWebhookEvent(String payload, String sigHeader, String secret);

    StripeWebhookEvent parseAndValidateConnectWebhookEvent(String payload, String sigHeader, String secret);
}
