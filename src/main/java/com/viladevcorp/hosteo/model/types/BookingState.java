package com.viladevcorp.hosteo.model.types;

public enum BookingState {
  PENDING,
  IN_PROGRESS,
  FINISHED,
  CANCELLED;

  public boolean isInProgress() {
    return this == IN_PROGRESS;
  }

  public boolean isPending() {
    return this == PENDING;
  }

  public boolean isCancelled() {
    return this == CANCELLED;
  }

  public boolean isFinished() {
    return this == FINISHED;
  }
}
