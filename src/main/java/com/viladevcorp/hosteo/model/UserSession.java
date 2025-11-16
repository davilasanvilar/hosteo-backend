package com.viladevcorp.hosteo.model;

import java.util.Calendar;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_sessions")
@NoArgsConstructor
@Getter
@Setter
public class UserSession extends BaseEntity {

    Calendar deletedAt;

    public UserSession(User user) {
        this.user = user;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

}
