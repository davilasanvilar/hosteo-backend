package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.types.CategoryEnum;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskSearchForm {

    private String name;

    private String apartmentName;

    private CategoryEnum category;

    private int pageNumber;

    private int pageSize;
}
