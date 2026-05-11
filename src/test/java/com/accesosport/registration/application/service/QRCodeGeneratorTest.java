package com.accesosport.registration.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QRCodeGeneratorTest {

    private final QRCodeGenerator generator = new QRCodeGenerator();

    // PNG magic bytes: 137 80 78 71 (hex 89 50 4E 47)
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 0x50, 0x4E, 0x47};

    @Test
    void generate_shouldReturnNonEmptyBytes() throws Exception {
        byte[] result = generator.generate("ACSP-4X7K", 150);

        assertThat(result).isNotNull().isNotEmpty();
    }

    @Test
    void generate_shouldReturnValidPngBytes() throws Exception {
        byte[] result = generator.generate("ACSP-4X7K", 150);

        assertThat(result).startsWith(PNG_MAGIC);
    }

    @Test
    void generate_withDifferentSizes_shouldReturnBytes() throws Exception {
        byte[] small = generator.generate("TEST", 100);
        byte[] large = generator.generate("TEST", 300);

        assertThat(small).isNotEmpty();
        assertThat(large).isNotEmpty();
        assertThat(large.length).isGreaterThan(small.length);
    }

    @Test
    void generate_withLongContent_shouldReturnBytes() throws Exception {
        String longContent = "ACSP-4X7K|participant-id-very-long|event-id-also-very-long";

        byte[] result = generator.generate(longContent, 200);

        assertThat(result).isNotEmpty().startsWith(PNG_MAGIC);
    }
}
