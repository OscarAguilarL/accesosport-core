package com.accesosport.shared.infrastructure.events;

import com.accesosport.shared.domain.events.DomainEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpringDomainEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private SpringDomainEventPublisher springDomainEventPublisher;

    @Test
    void publish_delegatesToApplicationEventPublisher() {
        DomainEvent event = new DomainEvent("test.event") {};

        springDomainEventPublisher.publish(event);

        verify(applicationEventPublisher, times(1)).publishEvent(event);
    }
}
