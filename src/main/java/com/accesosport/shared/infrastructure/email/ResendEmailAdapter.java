package com.accesosport.shared.infrastructure.email;

import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ResendEmailAdapter implements EmailService {

    private final EmailConfig emailConfig;
    private final RestClient resendRestClient;

    public ResendEmailAdapter(EmailConfig emailConfig,
                              @Qualifier("resendRestClient") RestClient resendRestClient) {
        this.emailConfig = emailConfig;
        this.resendRestClient = resendRestClient;
    }

    @Override
    public void send(EmailMessage message) {
        Map<String, Object> body = Map.of(
                "from", emailConfig.getFromEmail(),
                "to", List.of(message.to()),
                "subject", message.subject(),
                "html", message.htmlBody()
        );

        resendRestClient.post()
                .header("Authorization", "Bearer " + emailConfig.getApiKey())
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("[Email] Sent '{}' to {}", message.subject(), message.to());
    }

    @Override
    public void sendWithAttachment(String to, String subject, String htmlBody, String filename, byte[] content) {
        Map<String, Object> attachment = Map.of(
                "filename", filename,
                "content", Base64.getEncoder().encodeToString(content)
        );

        Map<String, Object> body = Map.of(
                "from", emailConfig.getFromEmail(),
                "to", List.of(to),
                "subject", subject,
                "html", htmlBody,
                "attachments", List.of(attachment)
        );

        resendRestClient.post()
                .header("Authorization", "Bearer " + emailConfig.getApiKey())
                .body(body)
                .retrieve()
                .toBodilessEntity();

        log.info("[Email] Sent '{}' with attachment '{}' to {}", subject, filename, to);
    }
}
