package com.accesosport.registration.domain.model;

import lombok.Getter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Getter
public class Registration {

    private UUID id;
    private UUID eventId;
    private UUID participantId;
    private UUID modalityId;
    private UUID categoryId;
    private RegistrationStatus status;
    private String ticketCode;
    private Integer bibNumber;
    private PaymentMethod paymentMethod;
    private boolean kitPickedUp;
    private LocalDateTime kitPickedUpAt;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime waiverAcceptedAt;
    private String waiverText;
    private boolean wantsShirt;

    // Token for anonymous participants to access their own payment flow
    private String paymentAccessTokenHash;
    private LocalDateTime paymentAccessTokenExpiresAt;

    // Participant snapshot — populated at registration time regardless of auth status
    private String participantEmail;
    private String participantFirstName;
    private String participantLastName;
    private String participantPhone;
    private String shirtSize;
    private String bloodType;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String medicalConditions;

    private Registration() {
    }

    public static Registration create(UUID eventId, UUID participantId, UUID modalityId, UUID categoryId,
                                      RegistrationStatus status,
                                      LocalDateTime waiverAcceptedAt, String waiverText, boolean wantsShirt,
                                      String participantEmail, String participantFirstName, String participantLastName,
                                      String participantPhone, String shirtSize, String bloodType,
                                      String emergencyContactName, String emergencyContactPhone,
                                      String medicalConditions) {
        Registration registration = new Registration();
        registration.id = UUID.randomUUID();
        registration.eventId = eventId;
        registration.participantId = participantId;
        registration.modalityId = modalityId;
        registration.categoryId = categoryId;
        registration.status = status;
        registration.ticketCode = TicketCodeGenerator.generate();
        registration.bibNumber = null;
        registration.paymentMethod = null;
        registration.kitPickedUp = false;
        registration.kitPickedUpAt = null;
        registration.registeredAt = LocalDateTime.now();
        registration.cancelledAt = null;
        registration.waiverAcceptedAt = waiverAcceptedAt;
        registration.waiverText = waiverText;
        registration.wantsShirt = wantsShirt;
        registration.participantEmail = participantEmail;
        registration.participantFirstName = participantFirstName;
        registration.participantLastName = participantLastName;
        registration.participantPhone = participantPhone;
        registration.shirtSize = shirtSize;
        registration.bloodType = bloodType;
        registration.emergencyContactName = emergencyContactName;
        registration.emergencyContactPhone = emergencyContactPhone;
        registration.medicalConditions = medicalConditions;
        return registration;
    }

    public static Registration reconstitute(
            UUID id,
            UUID eventId,
            UUID participantId,
            UUID modalityId,
            UUID categoryId,
            RegistrationStatus status,
            String ticketCode,
            Integer bibNumber,
            PaymentMethod paymentMethod,
            boolean kitPickedUp,
            LocalDateTime kitPickedUpAt,
            LocalDateTime registeredAt,
            LocalDateTime cancelledAt,
            LocalDateTime waiverAcceptedAt,
            String waiverText,
            boolean wantsShirt,
            String participantEmail,
            String participantFirstName,
            String participantLastName,
            String participantPhone,
            String shirtSize,
            String bloodType,
            String emergencyContactName,
            String emergencyContactPhone,
            String medicalConditions,
            String paymentAccessTokenHash,
            LocalDateTime paymentAccessTokenExpiresAt
    ) {
        Registration registration = new Registration();
        registration.id = id;
        registration.eventId = eventId;
        registration.participantId = participantId;
        registration.modalityId = modalityId;
        registration.categoryId = categoryId;
        registration.status = status;
        registration.ticketCode = ticketCode;
        registration.bibNumber = bibNumber;
        registration.paymentMethod = paymentMethod;
        registration.kitPickedUp = kitPickedUp;
        registration.kitPickedUpAt = kitPickedUpAt;
        registration.registeredAt = registeredAt;
        registration.cancelledAt = cancelledAt;
        registration.waiverAcceptedAt = waiverAcceptedAt;
        registration.waiverText = waiverText;
        registration.wantsShirt = wantsShirt;
        registration.participantEmail = participantEmail;
        registration.participantFirstName = participantFirstName;
        registration.participantLastName = participantLastName;
        registration.participantPhone = participantPhone;
        registration.shirtSize = shirtSize;
        registration.bloodType = bloodType;
        registration.emergencyContactName = emergencyContactName;
        registration.emergencyContactPhone = emergencyContactPhone;
        registration.medicalConditions = medicalConditions;
        registration.paymentAccessTokenHash = paymentAccessTokenHash;
        registration.paymentAccessTokenExpiresAt = paymentAccessTokenExpiresAt;
        return registration;
    }

    /** Generates a high-entropy token for anonymous participants. Returns the plain-text token
     *  (to be sent once in the response); stores only the SHA-256 hash. */
    public String generatePaymentAccessToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        this.paymentAccessTokenHash = sha256Hex(token);
        this.paymentAccessTokenExpiresAt = LocalDateTime.now().plusDays(30);
        return token;
    }

    /** Constant-time comparison of the provided token against the stored hash. */
    public boolean verifyPaymentAccessToken(String token) {
        if (token == null || paymentAccessTokenHash == null) return false;
        if (paymentAccessTokenExpiresAt != null && LocalDateTime.now().isAfter(paymentAccessTokenExpiresAt)) {
            return false;
        }
        return MessageDigest.isEqual(
                paymentAccessTokenHash.getBytes(),
                sha256Hex(token).getBytes()
        );
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    public void confirm(PaymentMethod paymentMethod) {
        if (this.status != RegistrationStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Registration must be in PENDING_PAYMENT state to confirm, but was: " + this.status);
        }
        this.status = RegistrationStatus.CONFIRMED;
        this.paymentMethod = paymentMethod;
    }

    public void cancel() {
        if (this.status == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Registration is already cancelled");
        }
        this.status = RegistrationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void assignPaymentMethod(PaymentMethod method) {
        if (method == null) {
            throw new IllegalArgumentException("Payment method cannot be null");
        }
        this.paymentMethod = method;
    }

    public void assignBibNumber(int number) {
        if (number <= 0) {
            throw new IllegalArgumentException("Bib number must be positive");
        }
        if (this.bibNumber != null) {
            throw new IllegalStateException("Bib number already assigned");
        }
        this.bibNumber = number;
    }

    public void markKitPickedUp() {
        if (this.kitPickedUp) {
            return;
        }
        this.kitPickedUp = true;
        this.kitPickedUpAt = LocalDateTime.now();
    }
}
