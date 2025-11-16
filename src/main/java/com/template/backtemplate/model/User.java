package com.template.backtemplate.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cascade;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(unique = true)
    @NotNull
    String email;

    @Column(unique = true)
    @NotNull
    String username;
    @NotNull
    String password;

    boolean validated = false;

@OneToMany(mappedBy = "user", orphanRemoval = true)
    Set<ValidationCode> validationCodes = new HashSet<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    Set<UserSession> userSessions = new HashSet<>();

    public User(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User user = (User) obj;
        return username.equals(user.username);
    }

    public int hashCode() {
        return username.hashCode();
    }

}
