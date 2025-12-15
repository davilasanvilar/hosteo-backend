package com.viladevcorp.hosteo.model.types;

public enum ApartmentState {
  READY,
  OCCUPIED,
  USED;

  public boolean isOccupied() {
    return this == OCCUPIED;
  }

  public boolean isReady() {
    return this == READY;
  }

  public boolean isUsed() {
    return this == USED;
  }
}
