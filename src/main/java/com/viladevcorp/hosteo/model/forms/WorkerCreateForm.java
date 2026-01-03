package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.Language;

import com.viladevcorp.hosteo.model.types.WorkerState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WorkerCreateForm {

  @NotNull @NotBlank private String name;

  private Language language;

  @NotNull private WorkerState state;

  private double salary;

  private boolean visible = true;
}
