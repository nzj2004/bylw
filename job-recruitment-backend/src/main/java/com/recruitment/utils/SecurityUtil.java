package com.recruitment.utils;

import com.recruitment.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        UserDetailsImpl user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static String getCurrentUsername() {
        UserDetailsImpl user = getCurrentUser();
        return user != null ? user.getUsername() : null;
    }

    public static Integer getCurrentRole() {
        UserDetailsImpl user = getCurrentUser();
        return user != null ? user.getRole() : null;
    }
}
