package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.*;
import com.viladevcorp.hosteo.model.types.ApartmentState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class ImportResultDto {

  public ImportResultDto(int successCount, int failureCount) {
    this.successCount = successCount;
    this.failureCount = failureCount;
  }

  private int successCount;
  private int failureCount;
}
