package com.wzp.cloud.support.infrastructure.event;

import com.wzp.cloud.support.shared.DomainEventPublisher;
import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GuavaDomainEventPublisher implements DomainEventPublisher {

    @Autowired
    EventBus eventBus;

    public void publish(Object event) {
        eventBus.post(event);
    }

}
