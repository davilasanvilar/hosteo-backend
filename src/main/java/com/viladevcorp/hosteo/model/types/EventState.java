package com.viladevcorp.hosteo.model.types;

public enum EventState {
  PENDING,
  IN_PROGRESS,
  FINISHED;

  public boolean isInProgress() {
    return this == IN_PROGRESS;
  }

  public boolean isPending() {
    return this == PENDING;
  }

  public boolean isFinished() {
    return this == FINISHED;
  }
}
