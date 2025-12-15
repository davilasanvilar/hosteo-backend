package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NextOfInProgressCannotBeFinishedOrInProgress extends Exception {
  public NextOfInProgressCannotBeFinishedOrInProgress(String message) {
    super(message);
  }
}
