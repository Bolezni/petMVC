package com.example.demo.api.config;

import com.example.demo.api.security.jwt.JwtFilter;
import com.example.demo.store.entities.Roles;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

import java.net.URI;
import java.util.Set;

@Slf4j
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

    /**
     * JwtFilter добавлен в цепочку безопасности вручную. Spring Boot по умолчанию
     * регистрирует любой бин-фильтр ещё и как обычный сервлет-фильтр, из-за чего
     * он выполнялся дважды: ранняя аутентификация заставляла SessionManagementFilter
     * применять CsrfAuthenticationStrategy на каждом запросе, CSRF-токен менялся,
     * и формы со старым токеном получали 403.
     */
    @Bean
    public org.springframework.boot.web.servlet.FilterRegistrationBean<JwtFilter> jwtFilterRegistration(
            JwtFilter filter) {
        var registration = new org.springframework.boot.web.servlet.FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){
        return http
                // CSRF включён: страницы выгружаются с сервера, аутентификация — по cookie.
                // Токен едет в cookie XSRF-TOKEN и в скрытом поле формы (Thymeleaf подставляет сам).
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .requireCsrfProtectionMatcher(new CsrfRequirementMatcher())
                        // по умолчанию здесь CsrfAuthenticationStrategy: она перевыпускает
                        // токен при каждой аутентификации. У нас аутентификация происходит
                        // на каждом запросе (JWT), поэтому токен менялся постоянно и формы
                        // со старым токеном получали 403. Сессий нет — стратегия не нужна.
                        .sessionAuthenticationStrategy((authentication, request, response) -> { }))
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
                        // файл защищён подписью в ссылке либо проверкой в контроллере (см. MediaController)
                        .requestMatchers(HttpMethod.GET, "/api/v1/media/*/content").permitAll()
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
                            // неверный/просроченный CSRF-токен: чаще всего это вкладка,
                            // открытая до обновления страницы, либо протухшая форма
                            if (e instanceof CsrfException) {
                                log.warn("CSRF-токен не принят: {} {} (cookie XSRF-TOKEN: {})",
                                        req.getMethod(), req.getRequestURI(), hasCsrfCookie(req));

                                if (isApi(req)) {
                                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                    res.setContentType("application/json;charset=UTF-8");
                                    res.getWriter().write("{\"error\":\"Форма устарела, обновите страницу\"}");
                                    return;
                                }

                                // возвращаем на ту же страницу с пометкой: страница
                                // перезагрузится, получит свежий токен и покажет баннер
                                String referer = req.getHeader("Referer");
                                if (isSameHost(req, referer)) {
                                    res.sendRedirect(withCsrfFlag(referer));
                                    return;
                                }

                                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                res.setContentType("text/html;charset=UTF-8");
                                res.getWriter().write("<!DOCTYPE html><html lang=\"ru\"><head>"
                                        + "<meta charset=\"UTF-8\"><title>403 — petMVP</title></head>"
                                        + "<body>"
                                        + "<h1>Форма устарела</h1>"
                                        + "<p>Страница была открыта до обновления сайта, поэтому защитный токен не совпал.</p>"
                                        + "<p>Обновите страницу (F5) и повторите действие.</p>"
                                        + "<p><a href=\"/\">На главную</a></p>"
                                        + "</body></html>");
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
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        // Сессий нет, защита от fixation не нужна. Без этого
                        // SessionManagementFilter на каждом запросе применяет
                        // CsrfAuthenticationStrategy и перевыпускает CSRF-токен,
                        // из-за чего формы со старым токеном получают 403.
                        .sessionAuthenticationStrategy((authentication, request, response) -> { }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

    }

    private boolean isApi(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    private boolean hasCsrfCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if ("XSRF-TOKEN".equals(cookie.getName()) && !cookie.getValue().isBlank()) {
                return true;
            }
        }
        return false;
    }

    /** Реферер должен вести на этот же хост, иначе редирект станет открытым. */
    private boolean isSameHost(HttpServletRequest request, String referer) {
        if (referer == null || referer.isBlank()) {
            return false;
        }
        try {
            String host = URI.create(referer).getHost();
            return host != null && host.equalsIgnoreCase(request.getServerName());
        } catch (RuntimeException ex) {
            return false;
        }
    }

    private String withCsrfFlag(String referer) {
        String flag = "csrf=expired";
        if (referer.contains("csrf=expired")) {
            return referer;
        }
        return referer + (referer.contains("?") ? "&" : "?") + flag;
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
