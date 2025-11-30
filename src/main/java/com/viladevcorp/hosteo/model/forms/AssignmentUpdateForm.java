package com.viladevcorp.hosteo.model.forms;

import java.time.Instant;
import java.util.UUID;

import com.viladevcorp.hosteo.model.types.TaskState;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentUpdateForm {

    @NotNull
    private UUID id;

    @NotNull
    private UUID taskId;

    @NotNull
    private Instant startDate;

    @NotNull
    private UUID workerId;

    @NotNull
    private TaskState state;

}
