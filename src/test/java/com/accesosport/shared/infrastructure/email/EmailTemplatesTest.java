package com.accesosport.shared.infrastructure.email;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplatesTest {

    @Test
    void registrationConfirmation_shouldContainTicketCode() {
        String html = EmailTemplates.registrationConfirmation(
                "Ana", "Maratón CDMX", "ACSP-1234", "42", "June 15, 2026 at 8:00 AM", "Chapultepec, CDMX");

        assertThat(html).contains("ACSP-1234");
    }

    @Test
    void registrationConfirmation_shouldContainEventNameAndParticipant() {
        String html = EmailTemplates.registrationConfirmation(
                "Ana", "Maratón CDMX", "ACSP-1234", "42", "June 15, 2026 at 8:00 AM", "Chapultepec, CDMX");

        assertThat(html).contains("Maratón CDMX");
        assertThat(html).contains("Ana");
    }

    @Test
    void registrationConfirmation_whenBibNumberPending_shouldShowPendingText() {
        String html = EmailTemplates.registrationConfirmation(
                "Ana", "Maratón CDMX", "ACSP-1234", "Pending assignment", "June 15, 2026 at 8:00 AM", "Chapultepec, CDMX");

        assertThat(html).contains("Pending assignment");
    }

    @Test
    void eventCancellation_shouldContainEventNameAndReason() {
        String html = EmailTemplates.eventCancellation(
                "Carlos", "Carrera 10K", "June 20, 2026 at 7:00 AM", "Condiciones climatológicas adversas");

        assertThat(html).contains("Carrera 10K");
        assertThat(html).contains("Condiciones climatológicas adversas");
        assertThat(html).contains("Carlos");
    }

    @Test
    void eventCancellation_shouldContainEventDate() {
        String html = EmailTemplates.eventCancellation(
                "Carlos", "Carrera 10K", "June 20, 2026 at 7:00 AM", "Razón");

        assertThat(html).contains("June 20, 2026 at 7:00 AM");
    }

    @Test
    void eventReminder_shouldContainBibNumberAndLocation() {
        String html = EmailTemplates.eventReminder(
                "Luis", "Ultra Trail", "July 1, 2026 at 6:00 AM", "Estadio, Guadalajara", "ACSP-7777", "101");

        assertThat(html).contains("101");
        assertThat(html).contains("Estadio, Guadalajara");
    }

    @Test
    void eventReminder_shouldContainTicketCodeAndParticipantName() {
        String html = EmailTemplates.eventReminder(
                "Luis", "Ultra Trail", "July 1, 2026 at 6:00 AM", "Estadio, Guadalajara", "ACSP-7777", "101");

        assertThat(html).contains("ACSP-7777");
        assertThat(html).contains("Luis");
    }
}
