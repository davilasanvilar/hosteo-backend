package com.viladevcorp.hosteo.common;

import java.text.SimpleDateFormat;
import java.time.Instant;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import com.viladevcorp.hosteo.model.User;
import com.viladevcorp.hosteo.repository.UserRepository;

public class TestUtils {

    public static void injectUserSession(String username, UserRepository userRepository) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
        } else {
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(null, null, null));
        }
    }

    public static Instant dateStrToInstant(String dateStr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.parse(dateStr).toInstant();
    }

}
