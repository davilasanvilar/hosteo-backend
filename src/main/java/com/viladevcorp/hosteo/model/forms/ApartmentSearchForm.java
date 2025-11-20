package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.ApartmentStateEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ApartmentSearchForm {

    private String name;
    private ApartmentStateEnum state;
    private int pageNumber;
    private int pageSize;

}
