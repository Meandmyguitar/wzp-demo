package com.demo.tracing;

import brave.Tracing;
import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.config.AbstractRabbitListenerContainerFactory;

import java.util.List;
import java.util.Map;

public final class TracingRabbitListenerAdvice implements MethodInterceptor {

    public static AbstractRabbitListenerContainerFactory<?> decorateRabbitListenerContainerFactory(
            AbstractRabbitListenerContainerFactory<?> factory,
            Tracing tracing
    ) {
        Advice[] chain = factory.getAdviceChain();

        TracingRabbitListenerAdvice tracingAdvice = new TracingRabbitListenerAdvice(tracing);
        // If there are no existing advice, return only the tracing one
        if (chain == null) {
            factory.setAdviceChain(tracingAdvice);
            return factory;
        }

        // If there is an existing tracing advice return
        for (Advice advice : chain) {
            if (advice instanceof TracingRabbitListenerAdvice) {
                return factory;
            }
        }

        // Otherwise, add ours and return
        Advice[] newChain = new Advice[chain.length + 1];
        System.arraycopy(chain, 0, newChain, 0, chain.length);
        newChain[chain.length] = tracingAdvice;
        factory.setAdviceChain(newChain);
        return factory;
    }

    private final Tracing tracing;

    private TracingRabbitListenerAdvice(Tracing tracing) {
        this.tracing = tracing;
    }

    @Override public Object invoke(MethodInvocation methodInvocation) {
        Message message = (Message) methodInvocation.getArguments()[1];

        return TracingUtils.withSpanInScope(tracing, message.getMessageProperties(), () -> {
            clearHeaders(message.getMessageProperties().getHeaders());
            return methodInvocation.proceed();
        });
    }

    private void clearHeaders(Map<String, Object> headers) {
        List<String> propagationKeys = tracing.propagation().keys();
        for (String propagationKey : propagationKeys) {
            headers.remove(propagationKey);
        }
    }
}
