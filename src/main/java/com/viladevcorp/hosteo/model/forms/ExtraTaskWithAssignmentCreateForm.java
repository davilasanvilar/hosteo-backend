package com.viladevcorp.hosteo.model.forms;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExtraTaskWithAssignmentCreateForm {

  @NotNull private ExtraAssignmentCreateForm assignmentCreateForm;

  @NotNull private ExtraTaskCreateForm taskCreateForm;
}
