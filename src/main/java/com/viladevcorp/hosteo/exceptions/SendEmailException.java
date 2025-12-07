package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SendEmailException extends Exception {
  public SendEmailException(String message) {
    super(message);
  }
}
