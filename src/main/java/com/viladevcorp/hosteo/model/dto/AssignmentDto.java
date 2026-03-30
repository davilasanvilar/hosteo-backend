package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.Assignment;
import com.viladevcorp.hosteo.model.types.AssignmentState;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.BeanUtils;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentDto extends BaseEntityDto {

  public AssignmentDto(Assignment assignment) {
    if (assignment == null) {
      return;
    }
    BeanUtils.copyProperties(assignment, this, "task", "worker");
    this.task = new TaskWithApartmentDto(assignment.getTask());
    this.worker = new WorkerDto(assignment.getWorker());
  }

  private TaskWithApartmentDto task;

  private Instant startDate;

  private Instant endDate;

  private WorkerDto worker;

  private AssignmentState state;
}
