package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.WorkerState;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WorkerSearchForm {

  private String name;
  private WorkerState state;
  private int pageNumber;
  private int pageSize;
}
