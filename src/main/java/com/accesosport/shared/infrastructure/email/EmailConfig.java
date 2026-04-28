package com.accesosport.shared.infrastructure.email;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@ConfigurationProperties(prefix = "resend")
@Data
public class EmailConfig {

    private static final String RESEND_EMAILS_URL = "https://api.resend.com/emails";

    private String apiKey;
    private String fromEmail;

    @Bean
    public RestClient resendRestClient() {
        return RestClient.builder()
                .baseUrl(RESEND_EMAILS_URL)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
