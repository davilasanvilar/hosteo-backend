package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.BaseEntity;
import com.viladevcorp.hosteo.model.Booking;
import com.viladevcorp.hosteo.model.Conflict;
import com.viladevcorp.hosteo.model.types.BookingSource;
import com.viladevcorp.hosteo.model.types.BookingState;
import java.time.Instant;
import java.util.*;

import com.viladevcorp.hosteo.model.types.ConflictType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class ConflictDto {

  public ConflictDto(Conflict conflict) {
    if (conflict == null) {
      return;
    }
    this.type = conflict.getType();
    BaseEntity conflictEntity = conflict.getConflictEntity();
    this.conflictEntity = conflictEntity != null ? conflictEntity.toDto() : null;
  }

  private ConflictType type;

  private BaseEntityDto conflictEntity;
}
