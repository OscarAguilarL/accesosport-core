package com.accesosport.registration.application.service;

import com.accesosport.event.domain.model.Event;
import com.accesosport.event.domain.model.Location;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.registration.domain.model.RegistrationStatus;
import com.accesosport.user.domain.model.PersonalData;
import com.accesosport.user.domain.model.User;
import com.accesosport.event.domain.model.DistanceUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TicketPdfGeneratorTest {

    @Mock private Event event;
    @Mock private Location location;

    private TicketPdfGenerator generator;

    // PDF magic bytes: %PDF
    private static final byte[] PDF_MAGIC = {0x25, 0x50, 0x44, 0x46};

    @BeforeEach
    void setUp() {
        // Use the real QRCodeGenerator — it has no external dependencies
        generator = new TicketPdfGenerator(new QRCodeGenerator());

        when(event.getName()).thenReturn("Maratón CDMX 2026");
        when(event.getEventDate()).thenReturn(LocalDateTime.of(2026, 6, 15, 8, 0));
        when(event.getLocation()).thenReturn(location);
        // distance is now passed as a String label — no longer on Event
        when(location.place()).thenReturn("Av. Reforma, CDMX");
        when(location.city()).thenReturn("CDMX");
    }

    @Test
    void generate_shouldReturnNonEmptyBytes() throws Exception {
        byte[] result = generator.generate(testRegistration(42), event, testUser(), "42.195 km");

        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void generate_shouldReturnValidPdfBytes() throws Exception {
        byte[] result = generator.generate(testRegistration(42), event, testUser(), "42.195 km");

        assertThat(result).startsWith(PDF_MAGIC);
    }

    @Test
    void generate_withNullBibNumber_shouldNotThrow() throws Exception {
        Registration reg = Registration.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                RegistrationStatus.CONFIRMED, "ACSP-TEST", null, null,
                false, null, LocalDateTime.now(), null);

        byte[] result = generator.generate(reg, event, testUser(), null);

        assertThat(result).isNotEmpty().startsWith(PDF_MAGIC);
    }

    @Test
    void generate_withNullPersonalData_shouldNotThrow() throws Exception {
        User userWithoutPersonalData = User.builder()
                .id(UUID.randomUUID())
                .email("sin-nombre@test.com")
                .build();

        byte[] result = generator.generate(testRegistration(10), event, userWithoutPersonalData, "10 km");

        assertThat(result).isNotEmpty().startsWith(PDF_MAGIC);
    }

    @Test
    void generate_withNullLocation_shouldNotThrow() throws Exception {
        when(event.getLocation()).thenReturn(null);

        byte[] result = generator.generate(testRegistration(5), event, testUser(), "5 km");

        assertThat(result).isNotEmpty().startsWith(PDF_MAGIC);
    }

    private Registration testRegistration(Integer bibNumber) {
        return Registration.reconstitute(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), null,
                RegistrationStatus.CONFIRMED, "ACSP-4X7K", bibNumber, null,
                false, null, LocalDateTime.now(), null);
    }

    private User testUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .email("participante@test.com")
                .personalData(PersonalData.builder()
                        .firstName("Juan")
                        .lastName("García")
                        .build())
                .build();
    }
}
