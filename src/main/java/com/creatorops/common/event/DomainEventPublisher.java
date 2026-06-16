package com.creatorops.common.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Publishes domain events to the Spring ApplicationEventPublisher context.
 */
@Component
public class DomainEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public DomainEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Publishes a business DomainEvent to the system listener registry.
     */
    public void publish(DomainEvent event) {
        eventPublisher.publishEvent(event);
    }
}
