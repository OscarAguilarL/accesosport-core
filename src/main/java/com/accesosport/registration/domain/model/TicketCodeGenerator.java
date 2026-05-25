package com.accesosport.registration.domain.model;

import java.security.SecureRandom;

public final class TicketCodeGenerator {

    // Sin O, 0, I, 1 — evita confusiones al leer en papel
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TicketCodeGenerator() {
    }

    public static String generate() {
        return randomSegment(4) + "-" + randomSegment(4); // "ACSP-4X7K"
    }

    private static String randomSegment(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
