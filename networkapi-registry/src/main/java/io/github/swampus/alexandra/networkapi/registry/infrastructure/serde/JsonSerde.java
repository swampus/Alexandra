package io.github.swampus.alexandra.networkapi.registry.infrastructure.serde;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public final class JsonSerde {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private JsonSerde() {}

    public static byte[] toBytes(Object value) {
        try {
            return MAPPER.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new IllegalStateException("JSON serialize failed", e);
        }
    }

    public static <T> T fromBytes(byte[] bytes, Class<T> type) {
        try {
            return MAPPER.readValue(bytes, type);
        } catch (IOException e) {
            throw new IllegalStateException("JSON deserialize failed", e);
        }
    }
}
