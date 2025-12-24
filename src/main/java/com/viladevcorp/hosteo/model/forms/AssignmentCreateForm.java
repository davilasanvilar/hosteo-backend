package com.viladevcorp.hosteo.model.forms;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentCreateForm extends BaseAssignmentCreateForm {

  @NotNull private UUID taskId;
}
