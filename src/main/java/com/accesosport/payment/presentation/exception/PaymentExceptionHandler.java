package com.accesosport.payment.presentation.exception;

import com.accesosport.payment.domain.exception.InvalidWebhookSignatureException;
import com.accesosport.payment.domain.exception.OrganizerStripeNotLinkedException;
import com.accesosport.payment.domain.exception.OxxoRefundNotSupportedException;
import com.accesosport.payment.domain.exception.PaymentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class PaymentExceptionHandler {

    @ExceptionHandler(InvalidWebhookSignatureException.class)
    public ResponseEntity<ProblemDetail> handleInvalidSignature(InvalidWebhookSignatureException ex) {
        log.warn("Stripe webhook signature validation failed");
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Invalid Webhook Signature");
        pd.setType(URI.create("https://api.accesosport.com/errors/invalid-webhook-signature"));
        pd.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ProblemDetail> handlePaymentNotFound(PaymentNotFoundException ex) {
        log.error("Payment not found: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Payment Not Found");
        pd.setType(URI.create("https://api.accesosport.com/errors/payment-not-found"));
        pd.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
    }

    @ExceptionHandler(OxxoRefundNotSupportedException.class)
    public ResponseEntity<ProblemDetail> handleOxxoRefund(OxxoRefundNotSupportedException ex) {
        log.warn("OXXO refund attempted: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("OXXO Refund Not Supported");
        pd.setType(URI.create("https://api.accesosport.com/errors/oxxo-refund-not-supported"));
        pd.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
    }

    @ExceptionHandler(OrganizerStripeNotLinkedException.class)
    public ResponseEntity<ProblemDetail> handleNotLinked(OrganizerStripeNotLinkedException ex) {
        log.warn("Organizer Stripe account not linked: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        pd.setTitle("Stripe Account Not Linked");
        pd.setType(URI.create("https://api.accesosport.com/errors/stripe-not-linked"));
        pd.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
    }
}
