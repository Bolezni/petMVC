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
        addBackLink(model, me);
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
        addBackLink(model, me);

        if (bindingResult.hasErrors()) {
            return "app/profile";
        }

        try {
            userService.update(currentUser.user(), form);
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

    private static void addBackLink(Model model, UserDto me) {
        boolean isSpecialist = me.role() != null && me.role().name().equals("SPECIALIST");
        if (isSpecialist) {
            model.addAttribute("backUrl", "/specialist");
            model.addAttribute("backLabel", "← К списку пользователей");
        } else {
            model.addAttribute("backUrl", "/app");
            model.addAttribute("backLabel", "← В кабинет");
        }
    }
}
