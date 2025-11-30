package com.viladevcorp.hosteo.model.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TaskSearchForm {

    private String name;

    private String apartmentName;

    private int pageNumber;

    private int pageSize;
}
