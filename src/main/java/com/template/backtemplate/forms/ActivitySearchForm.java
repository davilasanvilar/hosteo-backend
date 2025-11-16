package com.template.backtemplate.forms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ActivitySearchForm {

    private String name;
    private int page;
    private int pageSize;

}
