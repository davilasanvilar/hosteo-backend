package com.viladevcorp.hosteo.model;

import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cascade;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @Column(nullable = false, unique = true)
    @NotNull
    private String email;

    @Column(nullable = false, unique = true)
    @NotNull
    private String username;
    
    @Column(nullable = false)
    @NotNull
    @JsonIgnore
    private String password;

    @JsonIgnore
    private boolean validated = false;

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @JsonIgnore
    Set<ValidationCode> validationCodes = new HashSet<>();

    @OneToMany(mappedBy = "user", orphanRemoval = true)
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    @JsonIgnore
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
