package com.example.demo.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS нужен только для API: MVC-страницы живут на том же origin, им он не требуется.
 *
 * Список origins пуст по умолчанию — тогда поведение прежнее, браузерные запросы
 * с чужих сайтов не проходят. Когда фронтенд переедет на отдельный домен,
 * достаточно задать CORS_ALLOWED_ORIGINS, код менять не нужно.
 *
 * allowCredentials включён всегда: если фронтенд на поддомене, cookie с токеном
 * должны доезжать до API. Из-за этого нельзя отдавать "*" в Allow-Origin —
 * при wildcard переключаемся на allowedOriginPatterns.
 */
@Configuration
public class CorsConfig {

    private final List<String> allowedOrigins;

    public CorsConfig(@Value("${app.cors.allowed-origins:}") String origins) {
        this.allowedOrigins = Arrays.stream(origins.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        if (allowedOrigins.contains("*")) {
            config.setAllowedOriginPatterns(List.of("*"));
        } else {
            config.setAllowedOrigins(allowedOrigins);
        }

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type", "Accept", "Origin", "X-Admin-Key"));
        config.setExposedHeaders(List.of("Location"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // страницы MVC не регистрируем: им CORS не нужен
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
