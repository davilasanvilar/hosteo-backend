package com.viladevcorp.hosteo.utils;

import org.springframework.security.core.context.SecurityContextHolder;

import com.viladevcorp.hosteo.exceptions.NotAllowedResourceException;
import com.viladevcorp.hosteo.model.BaseEntity;
import com.viladevcorp.hosteo.model.User;

public class AuthUtils {

    public static void checkIfCreator(BaseEntity entity) throws NotAllowedResourceException {
        boolean isCreator = checkIfLoggedUser(entity.getCreatedBy());
        if (isCreator) {
            return;
        } else {
            throw new NotAllowedResourceException("You are not allowed to access this resource");
        }
    }

    public static boolean checkIfLoggedUser(User user) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return user.getUsername().equals(currentUsername);
    }

    public static String getUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

}
