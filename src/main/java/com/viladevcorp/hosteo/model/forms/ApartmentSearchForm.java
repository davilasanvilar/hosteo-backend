package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.ApartmentState;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class ApartmentSearchForm {

  private String name;
  private List<ApartmentState> states;
  private int pageNumber;
  private int pageSize;
}
