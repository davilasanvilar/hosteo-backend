package com.viladevcorp.hosteo.model.jsonconverters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.viladevcorp.hosteo.model.Conflict;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter
public class ConflictJsonConverter implements AttributeConverter<Conflict, String> {
  private static final ObjectMapper objectMapper =
      new ObjectMapper().registerModule(new JavaTimeModule());

  @Override
  public String convertToDatabaseColumn(Conflict conflict) {
    try {
      return objectMapper.writeValueAsString(conflict);
    } catch (JsonProcessingException e) {
      log.error("[ConflictJsonConverter.converToDataBaseColumn] {}", e.getMessage());
      return null;
    }
  }

  @Override
  public Conflict convertToEntityAttribute(String jsonString) {
    try {
      return objectMapper.readValue(jsonString, Conflict.class);
    } catch (JsonProcessingException e) {
      log.error("[ConflictJsonConverter.convertToEntityAttribute] {}", e.getMessage());
      return null;
    }
  }
}
