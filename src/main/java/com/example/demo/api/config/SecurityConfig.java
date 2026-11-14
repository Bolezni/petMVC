package com.example.demo.api.config;

import com.example.demo.api.security.jwt.JwtFilter;
import com.example.demo.store.entities.Roles;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.Set;

@Configuration
@EnableMethodSecurity
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "TRACE", "OPTIONS");

    /** Эндпоинты, которые принимают JSON и не меняют состояние от имени пользователя. */
    private static final Set<String> CSRF_EXEMPT = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh"
    );

    private final JwtFilter jwtFilter;

    @Value("${app.cookies.secure:false}")
    private boolean cookieSecure;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        return http
                // CSRF включён: страницы выгружаются с сервера, аутентификация — по cookie.
                // Токен едет в cookie XSRF-TOKEN и в скрытом поле формы (Thymeleaf подставляет сам).
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(new CsrfRequirementMatcher()))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                // подключает CorsConfigurationSource из CorsConfig и пропускает preflight
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // страницы входа и статика доступны без токена
                        .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/favicon.ico").permitAll()
                        // создание специалиста защищено ключом X-Admin-Key, а не сессией
                        .requestMatchers("/admin", "/admin/**").permitAll()
                        .requestMatchers("/api/v1/specialist/**").hasAuthority(Roles.SPECIALIST.name())
                        .requestMatchers("/specialist/**").hasAuthority(Roles.SPECIALIST.name())
                        .requestMatchers("/api/v1/user/**").hasAuthority(Roles.USER.name())
                        .requestMatchers("/api/v1/admin/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            // страницы уводим на вход, API отвечает JSON
                            if (isApi(req)) {
                                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                res.setContentType("application/json;charset=UTF-8");
                                res.getWriter().write("{\"error\":\"Требуется авторизация\"}");
                                return;
                            }
                            res.sendRedirect("/login");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            // неверный/просроченный CSRF-токен — это не «нет прав»,
                            // редирект тут скрыл бы проблему от пользователя
                            if (e instanceof CsrfException) {
                                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                res.setContentType("text/html;charset=UTF-8");
                                res.getWriter().write("<h1>403</h1>"
                                        + "<p>Форма устарела. Обновите страницу и попробуйте снова.</p>");
                                return;
                            }

                            // пользователь без прав на /specialist возвращается в свой кабинет
                            if (!isApi(req)) {
                                res.sendRedirect("/app");
                                return;
                            }
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"Недостаточно прав\"}");
                        }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    private boolean isApi(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    /** Cookie с токеном: доступна JS (для будущего SPA), Secure и SameSite как у остальных. */
    private CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieCustomizer(cookie -> cookie
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/"));
        return repository;
    }

    /**
     * CSRF нужен везде, где состояние меняется по cookie:
     * - безопасные методы не трогаем;
     * - запросы с Authorization: Bearer не уязвимы к CSRF — чужой сайт не подставит заголовок;
     * - три JSON-эндпоинта аутентификации исключены: браузерная форма не может отправить
     *   application/json без preflight, а логин-страница всё равно шлёт токен.
     */
    static class CsrfRequirementMatcher implements RequestMatcher {
        @Override
        public boolean matches(HttpServletRequest request) {
            if (SAFE_METHODS.contains(request.getMethod())) {
                return false;
            }
            if (request.getHeader(HttpHeaders.AUTHORIZATION) != null) {
                return false;
            }
            return !CSRF_EXEMPT.contains(request.getRequestURI());
        }
    }
}
