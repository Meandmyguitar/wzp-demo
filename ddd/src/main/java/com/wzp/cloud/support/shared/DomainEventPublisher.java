package com.wzp.cloud.support.shared;

public interface DomainEventPublisher {
    public void publish(Object event);

}
