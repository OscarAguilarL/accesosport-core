package com.accesosport.shared.infrastructure.email;

import com.accesosport.shared.domain.model.EmailMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResendEmailAdapterTest {

    @Mock private EmailConfig emailConfig;
    @Mock private RestClient restClient;
    @Mock private RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock private RestClient.RequestBodySpec requestBodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    private ResendEmailAdapter adapter;

    @BeforeEach
    void setUp() {
        when(emailConfig.getFromEmail()).thenReturn("noreply@accesosport.com");
        when(emailConfig.getApiKey()).thenReturn("re_test_key");
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        doReturn(requestBodySpec).when(requestBodySpec).body((Object) any());
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(null);

        adapter = new ResendEmailAdapter(emailConfig, restClient);
    }

    @Test
    @SuppressWarnings("unchecked")
    void send_shouldCallResendWithCorrectBody() {
        EmailMessage message = EmailMessage.of("user@test.com", "Test Subject", "<p>Hello</p>");

        adapter.send(message);

        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(requestBodySpec).body(bodyCaptor.capture());
        Map<String, Object> body = bodyCaptor.getValue();

        assertThat(body.get("from")).isEqualTo("noreply@accesosport.com");
        assertThat(body.get("subject")).isEqualTo("Test Subject");
        assertThat(body.get("html")).isEqualTo("<p>Hello</p>");
    }

    @Test
    void send_shouldIncludeAuthorizationHeader() {
        EmailMessage message = EmailMessage.of("user@test.com", "Subject", "<p>Body</p>");

        adapter.send(message);

        verify(requestBodyUriSpec).header("Authorization", "Bearer re_test_key");
    }

    @Test
    void send_shouldPropagateExceptionFromRestClient() {
        RestClientResponseException apiError = mock(RestClientResponseException.class);
        when(responseSpec.toBodilessEntity()).thenThrow(apiError);

        EmailMessage message = EmailMessage.of("user@test.com", "Subject", "<p>Body</p>");

        assertThatThrownBy(() -> adapter.send(message))
                .isSameAs(apiError);
    }
}
