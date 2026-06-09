package com.accesosport.payment.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProcessorPort {

    record CreateCheckoutSessionCommand(
            UUID registrationId,
            String eventName,
            long amountTotalCentavos,
            long serviceFeeCentavos,
            String stripeAccountId,
            String successUrl,
            String cancelUrl
    ) {}

    record CheckoutSessionResult(String sessionId, String checkoutUrl) {}

    record RefundResult(String refundId) {}

    record ConnectAccountResult(String stripeAccountId) {}

    record OnboardingLinkResult(String url) {}

    record ConnectAccountStatus(boolean detailsSubmitted, boolean chargesEnabled) {}

    record StripeWebhookEvent(
            String type,
            String sessionId,
            String paymentIntentId,
            String paymentMethodType,
            String paymentStatus,
            String connectedAccountId,
            boolean detailsSubmitted
    ) {}

    CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand cmd);

    RefundResult refund(String paymentIntentId);

    ConnectAccountResult createConnectAccount(String email, String orgName);

    OnboardingLinkResult createOnboardingLink(String stripeAccountId, String returnUrl, String refreshUrl);

    ConnectAccountStatus getConnectAccountStatus(String stripeAccountId);

    StripeWebhookEvent parseAndValidateWebhookEvent(String payload, String sigHeader, String secret);
}
