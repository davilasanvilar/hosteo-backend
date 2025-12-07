package com.viladevcorp.hosteo.utils;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ApiResponse<T> {

  private T data;
  private String errorCode;
  private String errorMessage;
  private List<ValidationError> validationErrors;

  public ApiResponse(T data) {
    this.data = data;
  }

  public ApiResponse(String errorCode, String errorMessage) {
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  public ApiResponse(String errorMessage, List<ValidationError> validationErrors) {
    this.errorMessage = errorMessage;
    this.validationErrors = validationErrors;
  }
}
