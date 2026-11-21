package com.example.demo.web;

import com.example.demo.api.dto.UserDto;
import com.example.demo.api.dto.UserUpdateRequest;
import com.example.demo.api.exception.exeptions.ConflictException;
import com.example.demo.api.security.CustomUserDetails;
import com.example.demo.api.service.user.UserService;
import com.example.demo.store.entities.Gender;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Страница профиля пользователя (MVC).
 * Текущий пользователь берётся из SecurityContext — id из URL не используется,
 * поэтому IDOR исключён по конструкции.
 */
@Controller
@RequestMapping("/app")
@RequiredArgsConstructor
public class ProfilePageController {

    private final UserService userService;

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        UserDto me = UserDto.from(currentUser.user());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", toForm(me));
        }
        model.addAttribute("me", me);
        model.addAttribute("genders", Gender.values());
        return "app/profile";
    }

    @PostMapping("/profile")
    public String update(@AuthenticationPrincipal CustomUserDetails currentUser,
                         @Valid @ModelAttribute("form") UserUpdateRequest form,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes flash) {
        UserDto me = UserDto.from(currentUser.user());
        model.addAttribute("me", me);
        model.addAttribute("genders", Gender.values());

        if (bindingResult.hasErrors()) {
            // форму вернём как есть — Thymeleaf подсветит ошибки валидации
            return "app/profile";
        }

        try {
            UserDto updated = userService.update(currentUser.user(), form);
            flash.addFlashAttribute("successMessage", "Профиль обновлён");
            return "redirect:/app/profile";
        } catch (ConflictException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "app/profile";
        }
    }

    private UserUpdateRequest toForm(UserDto dto) {
        return new UserUpdateRequest(
                nullToEmpty(dto.name()),
                nullToEmpty(dto.surname()),
                nullToEmpty(dto.email()),
                nullToEmpty(dto.phone()),
                dto.gender(),
                dto.birthday()
        );
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
