package com.accesosport.shared.infrastructure.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateServiceTest {

    private EmailTemplateService service;

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        service = new EmailTemplateService(engine);
    }

    @Test
    void registrationConfirmation_shouldContainTicketCode() {
        String html = service.registrationConfirmation(
                "Ana", "Maratón CDMX", "ACSP-1234", "42", "15 de junio de 2026, 8:00 AM", "Chapultepec, CDMX");

        assertThat(html).contains("ACSP-1234");
    }

    @Test
    void registrationConfirmation_shouldContainEventNameAndParticipant() {
        String html = service.registrationConfirmation(
                "Ana", "Maratón CDMX", "ACSP-1234", "42", "15 de junio de 2026, 8:00 AM", "Chapultepec, CDMX");

        assertThat(html).contains("Maratón CDMX");
        assertThat(html).contains("Ana");
    }

    @Test
    void registrationConfirmation_whenBibPending_shouldShowText() {
        String html = service.registrationConfirmation(
                "Ana", "Maratón CDMX", "ACSP-1234", "Sin asignar", "15 de junio de 2026, 8:00 AM", "Chapultepec, CDMX");

        assertThat(html).contains("Sin asignar");
    }

    @Test
    void eventCancellation_shouldContainEventNameAndReason() {
        String html = service.eventCancellation(
                "Carlos", "Carrera 10K", "20 de junio de 2026, 7:00 AM", "Condiciones climatológicas adversas");

        assertThat(html).contains("Carrera 10K");
        assertThat(html).contains("Condiciones climatológicas adversas");
        assertThat(html).contains("Carlos");
    }

    @Test
    void eventCancellation_shouldContainEventDate() {
        String html = service.eventCancellation(
                "Carlos", "Carrera 10K", "20 de junio de 2026, 7:00 AM", "Razón");

        assertThat(html).contains("20 de junio de 2026, 7:00 AM");
    }

    @Test
    void eventReminder_shouldContainBibNumberAndLocation() {
        String html = service.eventReminder(
                "Luis", "Ultra Trail", "1 de julio de 2026, 6:00 AM", "Estadio, Guadalajara", "ACSP-7777", "101");

        assertThat(html).contains("101");
        assertThat(html).contains("Estadio, Guadalajara");
    }

    @Test
    void eventReminder_shouldContainTicketCodeAndParticipantName() {
        String html = service.eventReminder(
                "Luis", "Ultra Trail", "1 de julio de 2026, 6:00 AM", "Estadio, Guadalajara", "ACSP-7777", "101");

        assertThat(html).contains("ACSP-7777");
        assertThat(html).contains("Luis");
    }
}
