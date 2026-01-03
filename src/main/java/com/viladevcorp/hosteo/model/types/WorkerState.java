package com.viladevcorp.hosteo.model.types;

public enum WorkerState {
  AVAILABLE,
  AWAY;

  public boolean isAvailable() {
    return this == AVAILABLE;
  }

  public boolean isAway() {
    return this == AWAY;
  }
}
