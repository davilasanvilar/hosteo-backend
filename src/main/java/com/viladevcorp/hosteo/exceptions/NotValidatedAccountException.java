package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class NotValidatedAccountException extends Exception {
  public NotValidatedAccountException(String message) {
    super(message);
  }
}
