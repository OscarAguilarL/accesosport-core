package com.accesosport.shared.presentation.rest;

import com.accesosport.shared.domain.model.EmailMessage;
import com.accesosport.shared.domain.port.EmailService;
import com.accesosport.shared.infrastructure.email.EmailTemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/contact")
@RequiredArgsConstructor
public class ContactController {

    private static final String CONTACT_INBOX = "contacto@accesosport.com";

    private final EmailService emailService;
    private final EmailTemplateService emailTemplateService;

    public record ContactRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(max = 2000) String message
    ) {}

    @PostMapping
    public ResponseEntity<Void> contact(@Valid @RequestBody ContactRequest request) {
        emailService.send(EmailMessage.of(
                CONTACT_INBOX,
                "Nuevo mensaje de contacto — " + request.name(),
                emailTemplateService.contactForm(request.name(), request.email(), request.message())
        ));
        return ResponseEntity.ok().build();
    }
}
