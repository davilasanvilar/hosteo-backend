package com.template.backtemplate.model;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "activities")
@Getter
@Setter
@NoArgsConstructor
public class Activity extends BaseEntity {

    @NotNull
    String name;

    String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    public Activity(String name, String description, User user) {
        this.name = name;
        this.description = description;
        this.user = user;
    }
}
