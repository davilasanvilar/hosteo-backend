package com.template.backtemplate.forms;

import com.template.backtemplate.model.User;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CreateActivityForm {

    private String name;
    private String description;
    private User user;
}
