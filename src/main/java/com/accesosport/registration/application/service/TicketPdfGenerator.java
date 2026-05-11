package com.accesosport.registration.application.service;

import com.accesosport.event.domain.model.Event;
import com.accesosport.registration.domain.model.Registration;
import com.accesosport.user.domain.model.User;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketPdfGenerator {

    private final QRCodeGenerator qrCodeGenerator;

    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 40f;
    private static final float HEADER_HEIGHT = 90f;
    private static final float LOGO_WIDTH = 400f;
    private static final float LOGO_HEIGHT = HEADER_HEIGHT;
    private static final float QR_SIZE = 150f;
    private static final float DATA_CARD_Y = PAGE_HEIGHT - 410f;
    private static final float DATA_CARD_HEIGHT = 220f;
    private static final float DATA_CARD_PADDING = 20f;
    private static final Color BRAND_COLOR = new Color(3, 55, 102);
    private static final Color LIGHT_GRAY = new Color(245, 245, 245);
    private static final Color DARK_GRAY = new Color(80, 80, 80);
    private static final Color MID_GRAY = new Color(100, 100, 100);
    private static final Color DIVIDER_GRAY = new Color(200, 200, 200);
    private static final Color MUTED_GRAY = new Color(160, 160, 160);

    public byte[] generate(Registration registration, Event event, User participant) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            // Fonts created once per PDF generation — reused across all drawing methods
            PDType1Font bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDType1Font regular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            byte[] qrBytes = qrCodeGenerator.generate(registration.getTicketCode(), (int) QR_SIZE);
            PDImageXObject qrImage = PDImageXObject.createFromByteArray(doc, qrBytes, "qr");

            PDImageXObject logoImage = loadLogo(doc);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                drawBackground(cs);
                drawHeader(cs, bold, regular, logoImage);
                drawEventSection(cs, event, bold, regular);
                drawParticipantSection(cs, registration, event, participant, bold, regular);
                drawQrCode(cs, qrImage, registration, bold);
                drawFooter(cs, bold, regular);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        } catch (WriterException e) {
            throw new IOException("Error al generar el código QR", e);
        }
    }

    private PDImageXObject loadLogo(PDDocument doc) {
        try (InputStream is = getClass().getResourceAsStream("/assets/logo-white.png")) {
            if (is == null) return null;
            return PDImageXObject.createFromByteArray(doc, is.readAllBytes(), "logo");
        } catch (IOException e) {
            log.warn("Logo no disponible, se omitirá del boleto: {}", e.getMessage());
            return null;
        }
    }

    private void drawBackground(PDPageContentStream cs) throws IOException {
        cs.setNonStrokingColor(BRAND_COLOR);
        cs.addRect(0, PAGE_HEIGHT - HEADER_HEIGHT, PAGE_WIDTH, HEADER_HEIGHT);
        cs.fill();

        cs.setNonStrokingColor(LIGHT_GRAY);
        cs.addRect(MARGIN, DATA_CARD_Y, PAGE_WIDTH - MARGIN * 2, DATA_CARD_HEIGHT);
        cs.fill();
    }

    private void drawHeader(PDPageContentStream cs, PDType1Font bold, PDType1Font regular,
                            PDImageXObject logoImage) throws IOException {
        float logoCenterY = PAGE_HEIGHT - HEADER_HEIGHT / 2f;

        if (logoImage != null) {
            float aspect = (float) logoImage.getWidth() / logoImage.getHeight();
            float drawW = LOGO_WIDTH;
            float drawH = drawW / aspect;
            if (drawH > LOGO_HEIGHT) {
                drawH = LOGO_HEIGHT;
                drawW = drawH * aspect;
            }
            float logoY = PAGE_HEIGHT - HEADER_HEIGHT + (HEADER_HEIGHT - drawH) / 2f;
            logoCenterY = logoY + drawH / 2f;
            cs.drawImage(logoImage, MARGIN, logoY, drawW, drawH);
        } else {
            cs.setNonStrokingColor(Color.WHITE);
            cs.beginText();
            cs.setFont(bold, 22);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 55f);
            cs.showText("AccesoSport");
            cs.endText();
        }

        float labelFontSize = 11f;
        float labelWidth = regular.getStringWidth("BOLETO DIGITAL") / 1000f * labelFontSize;
        cs.setNonStrokingColor(Color.WHITE);
        cs.beginText();
        cs.setFont(regular, labelFontSize);
        cs.newLineAtOffset(PAGE_WIDTH - MARGIN - labelWidth, logoCenterY - labelFontSize / 2f);
        cs.showText("BOLETO DIGITAL");
        cs.endText();
    }

    private void drawEventSection(PDPageContentStream cs, Event event,
                                  PDType1Font bold, PDType1Font regular) throws IOException {
        cs.setNonStrokingColor(Color.BLACK);
        cs.beginText();
        cs.setFont(bold, 16);
        cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 125f);
        cs.showText(truncate(event.getName().toUpperCase(), 55));
        cs.endText();

        cs.beginText();
        cs.setFont(regular, 11);
        cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 148f);
        cs.showText(formatEventDate(event));
        cs.endText();

        if (event.getLocation() != null && event.getLocation().place() != null) {
            cs.setNonStrokingColor(DARK_GRAY);
            cs.beginText();
            cs.setFont(regular, 10);
            cs.newLineAtOffset(MARGIN, PAGE_HEIGHT - 166f);
            cs.showText(truncate(event.getLocation().place(), 70));
            cs.endText();
        }
    }

    private void drawDivider(PDPageContentStream cs, float y) throws IOException {
        cs.setStrokingColor(DIVIDER_GRAY);
        cs.setLineWidth(0.5f);
        cs.moveTo(MARGIN, y);
        cs.lineTo(PAGE_WIDTH - MARGIN, y);
        cs.stroke();
    }

    private void drawParticipantSection(PDPageContentStream cs, Registration registration,
                                        Event event, User participant,
                                        PDType1Font bold, PDType1Font regular) throws IOException {
        float startY = PAGE_HEIGHT - 212f;
        float leftCol = MARGIN + DATA_CARD_PADDING;

        drawLabel(cs, bold, leftCol, startY, "PARTICIPANTE");
        drawValue(cs, bold, 13, leftCol, startY - 17f, truncate(getParticipantName(participant), 35));

        drawLabel(cs, bold, leftCol, startY - 47f, "DORSAL");
        String bibText = registration.getBibNumber() != null ? "# " + registration.getBibNumber() : "Sin asignar";
        drawValue(cs, bold, 20, leftCol, startY - 67f, bibText);

        drawLabel(cs, bold, leftCol, startY - 105f, "DISTANCIA");
        drawValue(cs, bold, 13, leftCol, startY - 121f,
                event.getDistance() != null ? event.getDistance().toString() : "-");

        drawLabel(cs, bold, leftCol, startY - 151f, "FOLIO");
        drawValue(cs, bold, 13, leftCol, startY - 167f, registration.getTicketCode());
    }

    private void drawLabel(PDPageContentStream cs, PDType1Font bold,
                           float x, float y, String text) throws IOException {
        cs.setNonStrokingColor(MID_GRAY);
        cs.beginText();
        cs.setFont(bold, 8);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawValue(PDPageContentStream cs, PDType1Font bold,
                           float fontSize, float x, float y, String text) throws IOException {
        cs.setNonStrokingColor(Color.BLACK);
        cs.beginText();
        cs.setFont(bold, fontSize);
        cs.newLineAtOffset(x, y);
        cs.showText(text);
        cs.endText();
    }

    private void drawQrCode(PDPageContentStream cs, PDImageXObject qrImage,
                            Registration registration, PDType1Font bold) throws IOException {
        float qrX = PAGE_WIDTH - MARGIN - QR_SIZE - DATA_CARD_PADDING;
        float qrY = PAGE_HEIGHT - 367f;

        cs.drawImage(qrImage, qrX, qrY, QR_SIZE, QR_SIZE);

        cs.setNonStrokingColor(DARK_GRAY);
        cs.beginText();
        cs.setFont(bold, 10);
        float codeWidth = registration.getTicketCode().length() * 6f;
        cs.newLineAtOffset(qrX + (QR_SIZE - codeWidth) / 2f, qrY - 16f);
        cs.showText(registration.getTicketCode());
        cs.endText();
    }

    private void drawFooter(PDPageContentStream cs, PDType1Font bold, PDType1Font regular) throws IOException {
        drawDivider(cs, 95f);

        cs.setNonStrokingColor(DARK_GRAY);
        cs.beginText();
        cs.setFont(bold, 9);
        cs.newLineAtOffset(MARGIN, 78f);
        cs.showText("Presenta este boleto para recoger tu kit deportivo");
        cs.endText();

        cs.beginText();
        cs.setFont(regular, 9);
        cs.newLineAtOffset(MARGIN, 63f);
        cs.showText("El día previo al evento · 8:00 AM - 8:00 PM");
        cs.endText();

        cs.setNonStrokingColor(MUTED_GRAY);
        cs.beginText();
        cs.setFont(regular, 8);
        cs.newLineAtOffset(MARGIN, 45f);
        cs.showText("AccesoSport — Sistema de ticketing para carreras atléticas");
        cs.endText();
    }

    private String formatEventDate(Event event) {
        if (event.getEventDate() == null) return "-";
        Locale esMx = new Locale("es", "MX");
        String dayOfWeek = event.getEventDate().getDayOfWeek().getDisplayName(TextStyle.FULL, esMx);
        String month = event.getEventDate().getMonth().getDisplayName(TextStyle.FULL, esMx);
        String time = event.getEventDate().format(DateTimeFormatter.ofPattern("h:mm a"));
        String capitalized = Character.toUpperCase(dayOfWeek.charAt(0)) + dayOfWeek.substring(1);
        return capitalized + " " + event.getEventDate().getDayOfMonth()
                + " de " + month + ", " + event.getEventDate().getYear() + " · " + time;
    }

    private String getParticipantName(User participant) {
        if (participant.getPersonalData() == null) return participant.getEmail();
        String first = participant.getPersonalData().getFirstName() != null
                ? participant.getPersonalData().getFirstName() : "";
        String last = participant.getPersonalData().getLastName() != null
                ? participant.getPersonalData().getLastName() : "";
        String full = (first + " " + last).trim();
        return full.isEmpty() ? participant.getEmail() : full;
    }

    private String truncate(String text, int maxChars) {
        if (text == null) return "";
        return text.length() <= maxChars ? text : text.substring(0, maxChars - 3) + "...";
    }
}
