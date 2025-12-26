package com.viladevcorp.hosteo.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.viladevcorp.hosteo.model.dto.*;
import com.viladevcorp.hosteo.model.types.ConflictType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class Conflict {

  private ConflictType type;

  @JsonTypeInfo(
      use = JsonTypeInfo.Id.NAME,
      include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
      property = "type")
  @JsonSubTypes({
    @JsonSubTypes.Type(value = AssignmentDto.class, name = ASSIGNMENT_CONFLICT_CONST),
    @JsonSubTypes.Type(value = ImpBookingDto.class, name = IMPORT_BOOKING_CONFLICT_CONST),
    @JsonSubTypes.Type(value = BookingDto.class, name = BOOKING_CONFLICT_CONST)
  })
  private BaseEntityDto conflictEntity;

  public static final String ASSIGNMENT_CONFLICT_CONST = "ASSIGNMENT_CONFLICT";
  public static final String BOOKING_CONFLICT_CONST = "BOOKING_CONFLICT";
  public static final String IMPORT_BOOKING_CONFLICT_CONST = "IMPORT_BOOKING_CONFLICT";
}
