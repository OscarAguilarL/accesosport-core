package com.accesosport.payment.infrastructure.stripe;

import com.stripe.StripeClient;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import com.accesosport.payment.domain.exception.InvalidWebhookSignatureException;
import com.accesosport.payment.domain.exception.StripeGatewayException;
import com.accesosport.payment.domain.port.PaymentProcessorPort;
import com.stripe.param.v2.core.AccountCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StripePaymentGatewayImpl implements PaymentProcessorPort {

    private final StripeClient stripeClient;

    @Override
    public CheckoutSessionResult createCheckoutSession(CreateCheckoutSessionCommand cmd) {
        try {
            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(cmd.successUrl() + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cmd.cancelUrl())
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
                            .setApplicationFeeAmount(cmd.serviceFeeCentavos())
                            .setTransferData(SessionCreateParams.PaymentIntentData.TransferData.builder()
                                    .setDestination(cmd.stripeAccountId())
                                    .build())
                            .build())
                    .build();

            Session session = stripeClient.checkout().sessions().create(params);
            return new CheckoutSessionResult(session.getId(), session.getUrl());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to create checkout session", e);
        }
    }

    @Override
    public RefundResult refund(String paymentIntentId) {
        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(paymentIntentId)
                    .build();
            com.stripe.model.Refund refund = stripeClient.refunds().create(params);
            return new RefundResult(refund.getId());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to create refund for payment intent " + paymentIntentId, e);
        }
    }

    @Override
    public ConnectAccountResult createConnectAccount(String email, String orgName) {
        try {
            var stripeTransfers =
                    com.stripe.param.v2.core.AccountCreateParams.Configuration.Recipient.Capabilities.StripeBalance.StripeTransfers.builder()
                            .setRequested(true)
                            .build();
            var stripeBalance =
                    com.stripe.param.v2.core.AccountCreateParams.Configuration.Recipient.Capabilities.StripeBalance.builder()
                            .setStripeTransfers(stripeTransfers)
                            .build();
            var recipientCapabilities =
                    com.stripe.param.v2.core.AccountCreateParams.Configuration.Recipient.Capabilities.builder()
                            .setStripeBalance(stripeBalance)
                            .build();
            var recipient =
                    com.stripe.param.v2.core.AccountCreateParams.Configuration.Recipient.builder()
                            .setCapabilities(recipientCapabilities)
                            .build();
            var identity =
                    com.stripe.param.v2.core.AccountCreateParams.Identity.builder()
                            .setCountry("MX")
                            .build();
            com.stripe.param.v2.core.AccountCreateParams params =
                    com.stripe.param.v2.core.AccountCreateParams.builder()
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
            com.stripe.model.v2.core.Account account = stripeClient.v2().core().accounts().create(params);
            return new ConnectAccountResult(account.getId());
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to create Stripe Connect account for " + email, e);
        }
    }

    @Override
    public OnboardingLinkResult createOnboardingLink(String stripeAccountId, String returnUrl, String refreshUrl) {
        try {
            AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                    .setAccount(stripeAccountId)
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .setReturnUrl(returnUrl)
                    .setRefreshUrl(refreshUrl)
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
            com.stripe.model.Account account = stripeClient.accounts().retrieve(stripeAccountId);
            return new ConnectAccountStatus(
                    Boolean.TRUE.equals(account.getDetailsSubmitted()),
                    Boolean.TRUE.equals(account.getChargesEnabled())
            );
        } catch (StripeException e) {
            throw new StripeGatewayException("Failed to retrieve account status for " + stripeAccountId, e);
        }
    }

    @Override
    public StripeWebhookEvent parseAndValidateWebhookEvent(String payload, String sigHeader, String secret) {
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
            String paymentMethodType = null;
            String paymentStatus = null;
            String connectedAccountId = event.getAccount();
            boolean detailsSubmitted = false;

            if ("checkout.session.completed".equals(event.getType())
                    || "checkout.session.async_payment_succeeded".equals(event.getType())
                    || "checkout.session.async_payment_failed".equals(event.getType())) {

                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new StripeGatewayException("Cannot deserialize checkout session", null));

                sessionId = session.getId();
                paymentIntentId = session.getPaymentIntent();
                paymentStatus = session.getPaymentStatus();

                if (session.getPaymentMethodTypes() != null && !session.getPaymentMethodTypes().isEmpty()) {
                    paymentMethodType = session.getPaymentMethodTypes().get(0);
                }
            } else if ("account.updated".equals(event.getType())) {
                com.stripe.model.Account account = (com.stripe.model.Account) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow(() -> new StripeGatewayException("Cannot deserialize account", null));
                connectedAccountId = account.getId();
                detailsSubmitted = Boolean.TRUE.equals(account.getDetailsSubmitted());
            }

            return new StripeWebhookEvent(
                    event.getType(),
                    sessionId,
                    paymentIntentId,
                    paymentMethodType,
                    paymentStatus,
                    connectedAccountId,
                    detailsSubmitted
            );
        } catch (StripeGatewayException e) {
            throw e;
        } catch (Exception e) {
            throw new StripeGatewayException("Failed to parse webhook event", e);
        }
    }
}
