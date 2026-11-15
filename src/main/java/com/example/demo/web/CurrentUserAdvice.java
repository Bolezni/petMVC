package com.example.demo.web;

import com.example.demo.store.entities.Roles;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Даёт шаблонам флаги пользователя, чтобы хедер показывал нужную навигацию.
 *
 * #authentication из Thymeleaf здесь недоступен: он приходит из extras-библиотеки
 * springsecurity6, которой в проекте нет. Поэтому состояние вычисляем сами —
 * иначе форма «Выйти» молча не рендерится вместе со всем блоком навигации.
 */
@ControllerAdvice
public class CurrentUserAdvice {

    @ModelAttribute("isSpecialist")
    public boolean isSpecialist() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> Roles.SPECIALIST.name().equals(a.getAuthority()));
    }

    @ModelAttribute("authenticated")
    public boolean authenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
