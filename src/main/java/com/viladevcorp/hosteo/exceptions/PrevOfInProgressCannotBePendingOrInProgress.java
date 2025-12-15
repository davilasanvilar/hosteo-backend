package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PrevOfInProgressCannotBePendingOrInProgress extends Exception {
  public PrevOfInProgressCannotBePendingOrInProgress(String message) {
    super(message);
  }
}
