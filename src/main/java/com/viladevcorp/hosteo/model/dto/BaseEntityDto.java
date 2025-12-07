package com.viladevcorp.hosteo.model.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.viladevcorp.hosteo.model.BaseEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseEntityDto {

  UUID id;
  Instant createdAt;
  UserDto createdBy;

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    BaseEntityDto other = (BaseEntityDto) obj;
    return this.id.equals(other.id);
  }

  public BaseEntityDto(BaseEntity baseEntity) {
    this.id = baseEntity.getId();
    this.createdAt = baseEntity.getCreatedAt();
    this.createdBy =
        baseEntity.getCreatedBy() != null ? new UserDto(baseEntity.getCreatedBy()) : null;
  }
}
