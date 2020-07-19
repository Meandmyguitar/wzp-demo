package com.wzp.cloud.support.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;

import java.io.IOException;

public class EnvelopeJsonConverter implements MessageConverter {

    private ClassLoader classLoader;

    public EnvelopeJsonConverter(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        try {
            if (!(object instanceof Envelope)) {
                throw new IllegalStateException("只能发送Envelope对象");
            }
            Envelope envelope = (Envelope) object;
            return new Message(Utils.toBytes(envelope.getPayload()), new MessageProperties());
        } catch (JsonProcessingException e) {
            throw new MessageConversionException(e.getMessage(), e);
        }
    }

    @Override
    public Object fromMessage(Message message) throws MessageConversionException {
        try {
            Payload payload = Utils.fromBytes(message.getBody(), classLoader);
            return new Envelope(payload.getData());
        } catch (ClassNotFoundException | IOException e) {
            throw new MessageConversionException(e.getMessage(), e);
        }
    }
}
