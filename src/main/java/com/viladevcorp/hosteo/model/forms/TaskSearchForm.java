package com.viladevcorp.hosteo.model.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TaskSearchForm {

  private String name;

  private UUID apartmentId;

  private boolean prepTask;

  private int pageNumber;

  private int pageSize;
}
