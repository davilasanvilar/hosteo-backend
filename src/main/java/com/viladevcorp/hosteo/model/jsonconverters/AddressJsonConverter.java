package com.viladevcorp.hosteo.model.jsonconverters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.model.Address;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AddressJsonConverter implements AttributeConverter<Address, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Address address) {
        try {
            return objectMapper.writeValueAsString(address);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Address convertToEntityAttribute(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, Address.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

}
