package com.demo.shared;

public interface DomainEventPublisher {
    public void publish(Object event);

}
