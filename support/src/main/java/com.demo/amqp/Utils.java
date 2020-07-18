package com.lanmaoly.cloud.support.amqp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lanmaoly.cloud.support.JacksonBuilder;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

class Utils {

    private static ObjectMapper mapper = JacksonBuilder.build();

    static byte[] toBytes(Object message) throws JsonProcessingException {

        JsonMessage msg = new JsonMessage();
        if (message instanceof Payload) {
            Payload payload = (Payload) message;
            Class<?> clazz = payload.getData().getClass();
            JsonNode node = mapper.convertValue(payload.getData(), JsonNode.class);

            msg.setClassName(clazz.getName());
            if (payload.getHeader().size() > 0) {
                msg.setHeader(payload.getHeader());
            }
            msg.setData(node);
        } else {
            Class<?> clazz = message.getClass();
            JsonNode node = mapper.convertValue(message, JsonNode.class);
            msg.setClassName(clazz.getName());
            msg.setData(node);
        }

        return mapper.writeValueAsBytes(msg);
    }

    static Payload fromBytes(byte[] data, ClassLoader loader) throws ClassNotFoundException, IOException {
        JsonMessage jsonMessage = mapper.readValue(data, JsonMessage.class);
        Class<?> clazz = loader.loadClass(jsonMessage.getClassName());
        Serializable serializable = (Serializable) mapper.convertValue(jsonMessage.getData(), clazz);
        return new Payload(serializable, jsonMessage.getHeader() == null ? Collections.emptyMap() : jsonMessage.getHeader());
    }

    static byte[] getBytes(ResultSet rs, String name) throws SQLException {
        Object o = rs.getObject(name);
        if (o instanceof Blob) {
            Blob blob = (Blob) o;
            byte[] data = blob.getBytes(1, (int) blob.length());
            blob.free();
            return data;
        } else if (o instanceof byte[]) {
            return (byte[]) o;
        } else {
            throw new IllegalStateException("未知的类型: " + o);
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static class JsonMessage {

        private String className;

        private Map<String, String> header;

        private JsonNode data;

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Map<String, String> getHeader() {
            return header;
        }

        public void setHeader(Map<String, String> header) {
            this.header = header;
        }

        public JsonNode getData() {
            return data;
        }

        public void setData(JsonNode data) {
            this.data = data;
        }
    }
}
