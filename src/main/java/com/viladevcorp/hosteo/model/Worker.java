package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.model.forms.WorkerCreateForm;
import com.viladevcorp.hosteo.model.types.Language;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "workers")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class Worker extends BaseEntity {

    public Worker(WorkerCreateForm form) {
        this.name = form.getName();
        this.language = form.getLanguage();
        this.salary = form.getSalary();
        this.visible = form.isVisible();
        this.setCreatedBy(form.getCreatedBy());
    }

    @NotNull
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private Language language;

    private double salary;

    private boolean visible;

}
