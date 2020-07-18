package com.lanmaoly.cloud.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.lanmaoly.util.lang.TimeUtils;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;

public class JacksonBuilder {

    public static ObjectMapper build() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(new CustomModule());
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        return mapper;
    }

    static class CustomModule extends SimpleModule {
        CustomModule() {

            addSerializer(Date.class, new DateSerializer());
            addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
            addSerializer(byte[].class, new ByteArraySerializer());

            addDeserializer(Date.class, new DateDeserializer());
            addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
            addDeserializer(byte[].class, new ByteArrayDeserializer());
        }
    }

    static class DateSerializer extends JsonSerializer<Date> {

        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(value.getTime());
        }
    }

    static class DateDeserializer extends JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return new Date(p.getValueAsLong());
        }
    }

    static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {

        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeNumber(value.toInstant(TimeUtils.DEFAULT_ZONE_OFFSET).toEpochMilli());
        }
    }

    static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(p.getValueAsLong()), TimeUtils.DEFAULT_ZONE_OFFSET);
        }
    }

    static class ByteArraySerializer extends JsonSerializer<byte[]> {

        @Override
        public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(Base64.getEncoder().encodeToString(value));
        }
    }

    static class ByteArrayDeserializer extends JsonDeserializer<byte[]> {

        @Override
        public byte[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String s = p.getValueAsString();
            return Base64.getDecoder().decode(s);
        }
    }
}
