package com.accesosport.shared.domain.port;

import com.accesosport.shared.domain.model.EmailMessage;

public interface EmailService {
    void send(EmailMessage message);
}
