package com.viladevcorp.hosteo.model.dto;

import com.viladevcorp.hosteo.model.*;
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
    BeanUtils.copyProperties(assignment, this, "task", "worker");
    this.task = new TaskDto(assignment.getTask());
    this.worker = new WorkerDto(assignment.getWorker());
  }

  private TaskDto task;

  private Instant startDate;

  private Instant endDate;

  private WorkerDto worker;

  private AssignmentState state;
}
