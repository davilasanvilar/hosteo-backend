package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NextOfPendingCannotBeInprogressOrFinished extends Exception {
  public NextOfPendingCannotBeInprogressOrFinished(String message) {
    super(message);
  }
}
