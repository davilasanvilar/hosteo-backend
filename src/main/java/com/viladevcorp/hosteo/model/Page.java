package com.viladevcorp.hosteo.model;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Page<T> {
  private List<T> content;
  private int totalPages;
  private long totalRows;

  public Page(List<T> content, int totalPages, long totalRows) {
    this.content = content;
    this.totalPages = totalPages;
    this.totalRows = totalRows;
  }
}
