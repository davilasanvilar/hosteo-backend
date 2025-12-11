package com.viladevcorp.hosteo.model.types;

public enum AssignmentState {
  PENDING,
  FINISHED;

  public boolean isFinished() {
    return this == FINISHED;
  }
}
