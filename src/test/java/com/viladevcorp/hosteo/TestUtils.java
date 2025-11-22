package com.viladevcorp.hosteo;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.repository.UserRepository;

public class TestUtils {

    public static void injectUserSession(String username, UserRepository userRepository) {
        User user = userRepository.findByUsername(username);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

}
