package com.example.demo.web;

import com.example.demo.api.dto.AuthenticationRequest;
import com.example.demo.api.dto.AuthenticationResponse;
import com.example.demo.api.dto.RegisterRequest;
import com.example.demo.api.exception.exeptions.ConflictException;
import com.example.demo.api.service.authentication.AuthenticationService;
import com.example.demo.api.service.authentication.RegisterService;
import com.example.demo.store.entities.Gender;
import com.example.demo.store.entities.Roles;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthPageController {

    private final AuthenticationService authenticationService;
    private final RegisterService registerService;
    private final AuthCookies authCookies;
    private final UserDetailsService userDetailsService;

    @GetMapping("/")
    public String landing() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error, Model model) {
        if (isAuthenticated()) {
            return redirectHome();
        }
        if (error != null) {
            model.addAttribute("error", "Неверный email или пароль");
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletResponse response) {
        try {
            AuthenticationResponse tokens = authenticationService.authentication(
                    new AuthenticationRequest(email.trim(), password));
            authCookies.write(response, tokens);
        } catch (Exception ex) {
            log.warn("Неудачный вход для {}: {}", email, ex.getMessage());
            return "redirect:/login?error";
        }
        return redirectHome(email.trim());
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(required = false) String error, Model model) {
        if (isAuthenticated()) {
            return redirectHome();
        }
        if (error != null) {
            model.addAttribute("error", "Пользователь с таким email или телефоном уже существует");
        }
        model.addAttribute("genders", Gender.values());
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String passwordRepeat,
                           @RequestParam String phone,
                           @RequestParam(required = false) String name,
                           @RequestParam Gender gender,
                           HttpServletResponse response) {
        RegisterRequest request = new RegisterRequest(
                email, password, passwordRepeat, phone, name, null, gender, null);

        try {
            registerService.register(request);
        } catch (ConflictException ex) {
            return "redirect:/register?error";
        } catch (RuntimeException ex) {
            log.warn("Ошибка регистрации {}: {}", email, ex.getMessage());
            return "redirect:/register?error";
        }

        // сразу входим, чтобы не заставлять вводить пароль второй раз
        try {
            AuthenticationResponse tokens = authenticationService.authentication(
                    new AuthenticationRequest(email.trim(), password));
            authCookies.write(response, tokens);
        } catch (Exception ex) {
            log.warn("Не удалось войти после регистрации {}: {}", email, ex.getMessage());
            return "redirect:/login";
        }

        return "redirect:/app/questionnaire";
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        authCookies.clear(response);
        return "redirect:/";
    }

    private String redirectHome() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean specialist = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> Roles.SPECIALIST.name().equals(a.getAuthority()));
        return specialist ? "redirect:/specialist" : "redirect:/app";
    }

    private String redirectHome(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        boolean specialist = userDetails.getAuthorities().stream()
                .anyMatch(a -> Roles.SPECIALIST.name().equals(a.getAuthority()));
        return specialist ? "redirect:/specialist" : "redirect:/app";
    }

    private boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
