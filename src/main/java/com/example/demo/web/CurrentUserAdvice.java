package com.example.demo.web;

import com.example.demo.store.entities.Roles;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Даёт шаблонам флаг роли, чтобы хедер показывал нужную навигацию.
 */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("isSpecialist")
    public boolean isSpecialist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> Roles.SPECIALIST.name().equals(a.getAuthority()));
    }
}
