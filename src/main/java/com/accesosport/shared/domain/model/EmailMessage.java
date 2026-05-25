package com.accesosport.shared.domain.model;

public record EmailMessage(
        String to,
        String subject,
        String htmlBody
) {
    public static EmailMessage of(String to, String subject, String htmlBody) {
        return new EmailMessage(to, subject, htmlBody);
    }
}
