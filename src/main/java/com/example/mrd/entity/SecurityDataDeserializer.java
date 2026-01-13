package com.example.mrd.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.kafka.common.serialization.Deserializer;

public class SecurityDataDeserializer implements Deserializer<SecurityData> {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SecurityDataDeserializer() {
        objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public SecurityData deserialize(String topic, byte[] data) {
        try {
            System.out.println("*** Deserializing SecurityData from Kafka message - " + new String(data) + " ***");
            return objectMapper.readValue(data, SecurityData.class);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during deserialization", e);
        }
    }

}
