package com.viladevcorp.hosteo.forms;

import com.viladevcorp.hosteo.model.types.ApartmentState;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ApartmentSearchForm {

    private String name;
    private ApartmentState state;
    private int pageNumber;
    private int pageSize;

}
