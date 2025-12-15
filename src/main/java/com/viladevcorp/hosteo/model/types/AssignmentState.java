package com.viladevcorp.hosteo.model.types;

public enum AssignmentState {
  PENDING,
  FINISHED;

  public boolean isPending() {
    return this == PENDING;
  }

  public boolean isFinished() {
    return this == FINISHED;
  }
}
