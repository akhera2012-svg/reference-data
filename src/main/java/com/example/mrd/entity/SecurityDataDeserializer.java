package com.example.mrd.entity;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.Deserializer;

public class SecurityDataDeserializer implements Deserializer<SecurityData> {
    private final ObjectMapper objectMapper = new ObjectMapper();

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
