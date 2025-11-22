package com.viladevcorp.hosteo.auth;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.utils.AuthUtils;

public class AuditorAwareImpl implements AuditorAware<User> {

    @Override
    public Optional<User> getCurrentAuditor() {
        return Optional.ofNullable(AuthUtils.getAuthUser());
    }
}
