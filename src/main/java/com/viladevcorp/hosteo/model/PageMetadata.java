package com.viladevcorp.hosteo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageMetadata {
  private int totalPages;
  private long totalRows;

  public PageMetadata(int totalPages, long totalRows) {
    this.totalPages = totalPages;
    this.totalRows = totalRows;
  }
}
