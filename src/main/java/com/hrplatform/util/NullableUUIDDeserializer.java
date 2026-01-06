package com.hrplatform.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.UUID;

public class NullableUUIDDeserializer extends JsonDeserializer<UUID> {

    @Override
    public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();

        if (value == null || value.trim().isEmpty() ||
                "null".equalsIgnoreCase(value.trim()) ||
                "all".equalsIgnoreCase(value.trim()) ||
                "alll".equalsIgnoreCase(value.trim())) {
            return null;
        }

        try {
            return UUID.fromString(value.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public UUID getNullValue(DeserializationContext ctxt) {
        return null;
    }
}