package com.viladevcorp.hosteo.model;

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

  private BaseEntity conflictEntity;
}
