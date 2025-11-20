package com.viladevcorp.hosteo.model.forms;

import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.model.types.LanguageEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class WorkerCreateForm {

    @NotNull
    @NotBlank
    private String name;

    private LanguageEnum language;

    private double salary;

    private boolean visible = false;

    private User createdBy;
}
