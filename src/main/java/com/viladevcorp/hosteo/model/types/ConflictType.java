package com.viladevcorp.hosteo.model.types;

import lombok.Getter;

import static com.viladevcorp.hosteo.model.Conflict.*;

@Getter
public enum ConflictType {
  ASSIGNMENT_CONFLICT(ASSIGNMENT_CONFLICT_CONST),
  BOOKING_CONFLICT(BOOKING_CONFLICT_CONST),
  IMPORT_BOOKING_CONFLICT(IMPORT_BOOKING_CONFLICT_CONST);

  private final String name;

  ConflictType(String name) {
    this.name = name;
  }
}
