package com.viladevcorp.hosteo.common;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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

    public static Calendar dateStrToCalendar(String dateStr) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(sdf.parse(dateStr));
        return calendarDate;
    }

}
