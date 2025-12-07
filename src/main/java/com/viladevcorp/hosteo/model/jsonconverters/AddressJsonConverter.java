package com.viladevcorp.hosteo.model.jsonconverters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viladevcorp.hosteo.model.Address;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class AddressJsonConverter implements AttributeConverter<Address, String> {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public String convertToDatabaseColumn(Address address) {
    try {
      return objectMapper.writeValueAsString(address);
    } catch (JsonProcessingException e) {
      log.error("[AddressJsonConverter.converToDataBaseColumn] {}", e.getMessage());
      return null;
    }
  }

  @Override
  public Address convertToEntityAttribute(String jsonString) {
    try {
      return objectMapper.readValue(jsonString, Address.class);
    } catch (JsonProcessingException e) {
      log.error("[AddressJsonConverter.convertToEntityAttribute] {}", e.getMessage());
      return null;
    }
  }
}
