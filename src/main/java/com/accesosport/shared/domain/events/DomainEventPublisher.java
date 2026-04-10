package com.accesosport.shared.domain.events;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
