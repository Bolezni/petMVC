package com.example.demo.web;

import com.example.demo.api.dto.RegisterRequest;
import com.example.demo.api.service.authentication.RegisterService;
import com.example.demo.store.entities.Gender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Служебная страница: создать специалиста может только тот, кто знает ADMIN_KEY.
 * Авторизация по ключу, а не по сессии — как у API-эндпоинта.
 */
@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminPageController {

    private final RegisterService registerService;

    @Value("${app.admin-key:}")
    private String adminKey;

    @GetMapping("/specialists")
    public String page(Model model) {
        model.addAttribute("genders", Gender.values());
        model.addAttribute("adminKeyRequired", !adminKey.isBlank());
        return "admin/specialists";
    }

    @PostMapping("/specialists")
    public String create(@RequestParam(required = false) String key,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String passwordRepeat,
                         @RequestParam String phone,
                         @RequestParam(required = false) String name,
                         @RequestParam Gender gender) {

        if (!isAllowed(key)) {
            return "redirect:/admin/specialists?error=key";
        }

        try {
            registerService.registerSpecialist(new RegisterRequest(
                    email, password, passwordRepeat, phone, name, null, gender, null));
        } catch (RuntimeException ex) {
            log.warn("Не удалось создать специалиста {}: {}", email, ex.getMessage());
            return "redirect:/admin/specialists?error=data";
        }

        return "redirect:/admin/specialists?created";
    }

    private boolean isAllowed(String key) {
        return !adminKey.isBlank() && key != null
                && MessageDigest.isEqual(
                adminKey.getBytes(StandardCharsets.UTF_8),
                key.getBytes(StandardCharsets.UTF_8));
    }
}
