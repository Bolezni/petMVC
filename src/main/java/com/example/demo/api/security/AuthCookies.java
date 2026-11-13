package com.example.demo.api.security;

import com.example.demo.api.dto.AuthenticationResponse;
import com.example.demo.api.security.jwt.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Кладёт пару токенов в httpOnly-cookie, чтобы страницы работали без JavaScript.
 * SameSite=Lax защищает от межсайтовых POST при отключённом CSRF.
 *
 * Cookie с access-токеном живёт столько же, сколько refresh: сам токен всё равно
 * проверяется по сроку, а долгоживущая cookie нужна, чтобы JwtFilter мог
 * незаметно обновить протухший access по refresh-токену.
 */
@Component
public class AuthCookies {

    public static final String REFRESH_COOKIE = "refresh_token";

    private final long accessTtlMs;
    private final long refreshTtlMs;

    public AuthCookies(@Value("${jwt.access-ttl-ms}") long accessTtlMs,
                       @Value("${jwt.refresh-ttl-ms}") long refreshTtlMs) {
        this.accessTtlMs = accessTtlMs;
        this.refreshTtlMs = refreshTtlMs;
    }

    public void write(HttpServletResponse response, AuthenticationResponse tokens) {
        add(response, JwtFilter.ACCESS_COOKIE, tokens.token(), refreshTtlMs);
        add(response, REFRESH_COOKIE, tokens.refreshToken(), refreshTtlMs);
    }

    /** Обновление одного access-токена без выхода из аккаунта. */
    public void writeAccess(HttpServletResponse response, String token) {
        add(response, JwtFilter.ACCESS_COOKIE, token, refreshTtlMs);
    }

    public void clear(HttpServletResponse response) {
        add(response, JwtFilter.ACCESS_COOKIE, "", 0);
        add(response, REFRESH_COOKIE, "", 0);
    }

    private void add(HttpServletResponse response, String name, String value, long ttlMs) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(ttlMs))
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
