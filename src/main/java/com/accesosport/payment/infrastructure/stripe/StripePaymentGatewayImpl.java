package com.accesosport.payment.infrastructure.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.net.Webhook;
import com.stripe.model.Event;
import com.stripe.model.v2.core.EventNotification;
import com.stripe.events.V2CoreAccountUpdatedEventNotification;
import com.stripe.events.V2CoreAccountIncludingConfigurationRecipientUpdatedEventNotification;
import com.stripe.events.V2CoreAccountIncludingConfigurationRecipientCapabilityStatusUpdatedEventNotification;
import com.stripe.events.V2CoreAccountIncludingRequirementsUpdatedEventNotification;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.PaymentIntentRetrieveParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.param.checkout.SessionRetrieveParams;
import com.accesosport.payment.domain.exception.InvalidWebhookSignatureException;
import com.accesosport.payment.domain.exception.StripeGatewayException;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.stripe.param.v2.core.AccountCreateParams;
import com.stripe.param.v2.core.AccountRetrieveParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripePaymentGatewayImpl implements PaymentProcessorPort {

    private final StripeClient stripeClient;

    @Value("${stripe.checkout-success-url}")
    private String checkoutSuccessUrl;

    @Value("${stripe.checkout-cancel-url}")
    private String checkoutCancelUrl;

    @Value("${stripe.onboarding-return-url}")
    private String onboardingReturnUrl;

    @Value("${stripe.onboarding-refresh-url}")
    private String onboardingRefreshUrl;

    @Value("${stripe.payment-method-configuration-id:}")
    private String paymentMethodConfigurationId;

    @Override
    public CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand cmd) {
        try {
            String successUrl = appendRegistrationId(checkoutSuccessUrl, cmd.registrationId());
            String cancelUrl = appendRegistrationId(checkoutCancelUrl, cmd.registrationId());

            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setLocale(SessionCreateParams.Locale.ES_419)
                    .setCurrency("mxn")
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("mxn")
                                    .setUnitAmount(cmd.amountTotalCentavos())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName(cmd.eventName())
                                            .build())
                                    .build())
                            .build())
                    .setPaymentIntentData(SessionCreateParams.PaymentIntentData.builder()
                            .setApplicationFeeAmount(cmd.serviceFeeCentavos() + cmd.organizerFeeCentavos())
                            .setTransferData(SessionCreateParams.PaymentIntentData.TransferData.builder()
                                    .setDestination(cmd.stripeAccountId())
                                    .build())
                            .putMetadata("registration_id", cmd.registrationId().toString())
                            .putMetadata("payment_id", cmd.paymentId().toString())
                            .putMetadata("event_id", cmd.eventId().toString())
                            .build());

            if (paymentMethodConfigurationId != null && !paymentMethodConfigurationId.isBlank()) {
                paramsBuilder.setPaymentMethodConfiguration(paymentMethodConfigurationId);
            }

            SessionCreateParams params = paramsBuilder.build();

            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey(cmd.idempotencyKey())
                    .build();

            Session session = stripeClient.checkout().sessions().create(params, options);
            return new CheckoutSessionResult(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to create checkout session", e);
        }
    }

    @Override
    public CheckoutSessionInfo retrieveCheckoutSession(String sessionId) {
        try {
            SessionRetrieveParams params = SessionRetrieveParams.builder().build();
            Session session = stripeClient.checkout().sessions().retrieve(sessionId, params);
            return new CheckoutSessionInfo(session.getUrl(), session.getStatus(), session.getPaymentIntent());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to retrieve checkout session " + sessionId, e);
        }
    }

    @Override
    public RefundResult refund(String paymentIntentId, UUID paymentId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setReverseTransfer(true)
                    .setRefundApplicationFee(true)
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("refund:" + paymentId)
                    .build();

            Refund refund = stripeClient.refunds().create(params, options);
            return new RefundResult(refund.getId());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to create refund for payment intent " + paymentIntentId, e);
        }
    }

    @Override
    public RefundResult refundPartial(String paymentIntentId, UUID paymentId, long amountCentavos) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .setAmount(amountCentavos)
                    .setReverseTransfer(true)
                    .setRefundApplicationFee(true)
                    .build();

            RequestOptions options = RequestOptions.builder()
                    .setIdempotencyKey("refund-partial:" + paymentId)
                    .build();

            Refund refund = stripeClient.refunds().create(params, options);
            return new RefundResult(refund.getId());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to create partial refund for payment intent " + paymentIntentId, e);
        }
    }

    @Override
    public String getActualPaymentMethod(String paymentIntentId) {
        try {
            PaymentIntentRetrieveParams params = PaymentIntentRetrieveParams.builder()
                    .addExpand("latest_charge")
                    .build();
            PaymentIntent pi = stripeClient.paymentIntents().retrieve(paymentIntentId, params);
            if (pi.getLatestChargeObject() == null) return null;
            if (pi.getLatestChargeObject().getPaymentMethodDetails() == null) return null;
            return pi.getLatestChargeObject().getPaymentMethodDetails().getType();
        } catch (StripeException e) {
            log.warn("Could not retrieve actual payment method for intent {}: {}", paymentIntentId, e.getMessage());
            return null;
        }
    }

    @Override
    public ConnectAccountResult createConnectAccount(String email, String orgName) {
        try {
            var stripeTransfers =
                    AccountCreateParams.Configuration.Recipient.Capabilities.StripeBalance.StripeTransfers.builder()
                            .setRequested(true)
                            .build();
            var stripeBalance =
                    AccountCreateParams.Configuration.Recipient.Capabilities.StripeBalance.builder()
                            .setStripeTransfers(stripeTransfers)
                            .build();
            var recipientCapabilities =
                    AccountCreateParams.Configuration.Recipient.Capabilities.builder()
                            .setStripeBalance(stripeBalance)
                            .build();
            var recipient =
                    AccountCreateParams.Configuration.Recipient.builder()
                            .setCapabilities(recipientCapabilities)
                            .build();
            var identity =
                    AccountCreateParams.Identity.builder()
                            .setCountry("MX")
                            .build();
            AccountCreateParams params =
                    AccountCreateParams.builder()
                            .setContactEmail(email)
                            .setDisplayName(orgName)
                            .setIdentity(identity)
                            .setDashboard(AccountCreateParams.Dashboard.EXPRESS)
                            .setConfiguration(
                                    AccountCreateParams.Configuration.builder()
                                            .setRecipient(recipient)
                                            .build()
                            )
                            .setDefaults(
                                    AccountCreateParams.Defaults.builder()
                                            .setResponsibilities(
                                                    AccountCreateParams.Defaults.Responsibilities.builder()
                                                            .setLossesCollector(
                                                                    AccountCreateParams.Defaults.Responsibilities.LossesCollector.APPLICATION
                                                            )
                                                            .setFeesCollector(
                                                                    AccountCreateParams.Defaults.Responsibilities.FeesCollector.APPLICATION
                                                            )
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();
            var account = stripeClient.v2().core().accounts().create(params);
            return new ConnectAccountResult(account.getId());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to create Stripe Connect account for " + email, e);
        }
    }

    @Override
    public OnboardingLinkResult createOnboardingLink(String stripeAccountId) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(stripeAccountId)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .setReturnUrl(onboardingReturnUrl)
                    .setRefreshUrl(onboardingRefreshUrl)
                    .build();
            com.stripe.model.AccountLink link = stripeClient.accountLinks().create(params);
            return new OnboardingLinkResult(link.getUrl());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to create onboarding link for account " + stripeAccountId, e);
        }
    }

    @Override
    public ConnectAccountStatus getConnectAccountStatus(String stripeAccountId) {
        try {
            AccountRetrieveParams params = AccountRetrieveParams.builder()
                    .addInclude(AccountRetrieveParams.Include.CONFIGURATION__RECIPIENT)
                    .addInclude(AccountRetrieveParams.Include.REQUIREMENTS)
                    .build();
            var account = stripeClient.v2().core().accounts().retrieve(stripeAccountId, params);

            boolean onboardingSubmitted = false;
            boolean transfersActive = false;
            boolean payoutsActive = false;
            boolean hasUserRequirements = false;
            boolean underStripeReview = false;

            var config = account.getConfiguration();
            if (config != null && config.getRecipient() != null) {
                var recipient = config.getRecipient();
                var caps = recipient.getCapabilities();
                if (caps != null && caps.getStripeBalance() != null) {
                    var stripeBalance = caps.getStripeBalance();
                    var transfers = stripeBalance.getStripeTransfers();
                    if (transfers != null) {
                        transfersActive = "active".equals(transfers.getStatus());
                    }
                    var payouts = stripeBalance.getPayouts();
                    if (payouts != null) {
                        payoutsActive = "active".equals(payouts.getStatus());
                    }
                }
                onboardingSubmitted = Boolean.TRUE.equals(recipient.getApplied());
            }

            if (account.getRequirements() != null && account.getRequirements().getEntries() != null) {
                hasUserRequirements = account.getRequirements().getEntries().stream()
                        .anyMatch(entry -> "user".equalsIgnoreCase(entry.getAwaitingActionFrom()));
                underStripeReview = account.getRequirements().getEntries().stream()
                        .anyMatch(entry -> "stripe".equalsIgnoreCase(entry.getAwaitingActionFrom()));
            }

            return new ConnectAccountStatus(
                    onboardingSubmitted, transfersActive, payoutsActive,
                    hasUserRequirements, underStripeReview);
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to retrieve account status for " + stripeAccountId, e);
        }
    }

    @Override
    public StripeWebhookEvent parseAndValidateCheckoutWebhookEvent(String payload, String sigHeader, String secret) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, secret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe webhook signature: {}", e.getMessage());
            throw new InvalidWebhookSignatureException();
        }

        try {
            String sessionId = null;
            String paymentIntentId = null;
            String paymentStatus = null;
            if ("checkout.session.completed".equals(event.getType())
                    || "checkout.session.async_payment_succeeded".equals(event.getType())
                    || "checkout.session.async_payment_failed".equals(event.getType())) {

                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new StripeGatewayException("Cannot deserialize checkout session", null));

                sessionId = session.getId();
                paymentIntentId = session.getPaymentIntent();
                paymentStatus = session.getPaymentStatus();
                // paymentMethodType is resolved via getActualPaymentMethod(paymentIntentId) in the use case

            }

            return new StripeWebhookEvent(
                    event.getId(),
                    event.getType(),
                    sessionId,
                    paymentIntentId,
                    null,  // resolved lazily via getActualPaymentMethod
                    paymentStatus,
                    null
            );
        } catch (StripeGatewayException e) {
            throw e;
        } catch (Exception e) {
            throw new StripeGatewayException("Failed to parse webhook event", e);
        }
    }

    @Override
    public StripeWebhookEvent parseAndValidateConnectWebhookEvent(
            String payload, String sigHeader, String secret) {
        EventNotification notification;
        try {
            notification = stripeClient.parseEventNotification(payload, sigHeader, secret);
        } catch (SignatureVerificationException e) {
            log.warn("Invalid Stripe Connect webhook signature: {}", e.getMessage());
            throw new InvalidWebhookSignatureException();
        }

        String accountId = switch (notification) {
            case V2CoreAccountUpdatedEventNotification event -> event.getRelatedObject().getId();
            case V2CoreAccountIncludingConfigurationRecipientUpdatedEventNotification event ->
                    event.getRelatedObject().getId();
            case V2CoreAccountIncludingConfigurationRecipientCapabilityStatusUpdatedEventNotification event ->
                    event.getRelatedObject().getId();
            case V2CoreAccountIncludingRequirementsUpdatedEventNotification event ->
                    event.getRelatedObject().getId();
            default -> null;
        };

        return new StripeWebhookEvent(
                notification.getId(), notification.getType(), null, null,
                null, null, accountId);
    }

    private String appendRegistrationId(String url, UUID registrationId) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "registration_id=" + registrationId;
    }
}
