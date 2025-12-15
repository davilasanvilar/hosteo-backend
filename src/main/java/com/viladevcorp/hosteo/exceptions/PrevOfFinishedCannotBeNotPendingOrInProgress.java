package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PrevOfFinishedCannotBeNotPendingOrInProgress extends Exception {
  public PrevOfFinishedCannotBeNotPendingOrInProgress(String message) {
    super(message);
  }
}
