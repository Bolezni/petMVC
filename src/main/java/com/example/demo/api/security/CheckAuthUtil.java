package com.example.demo.api.security;

import com.example.demo.store.entities.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

public class CheckAuthUtil {

    private CheckAuthUtil() {
    }

    public static boolean isCurrentUserAuthentication(Long targetId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            return false;

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            CustomUserDetails user = (CustomUserDetails) principal;
            return user.getId().equals(targetId);
        }
        return false;
    }

    public static UserEntity getAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated())
            throw new RuntimeException("Пользователь не авторизован");

        if (authentication.getPrincipal() instanceof CustomUserDetails(UserEntity user1)) {
            return user1;
        }

        throw new RuntimeException("Пользователь не найден");
    }
}
