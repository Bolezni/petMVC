package com.example.demo.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Простейший ограничитель попыток: не более 20 запросов в минуту
 * с одного IP на эндпоинт. Хранит состояние в памяти процесса —
 * при переходе на несколько инстансов заменить на Redis или лимит в Caddy.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 20;
    private static final long WINDOW_MS = 60_000L;
    private static final int MAX_KEYS = 10_000;

    /** Что ограничиваем: вход, регистрация, обновление токена и создание специалистов. */
    private static final List<String> LIMITED = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh",
            "/api/v1/admin/"
    );

    private final Map<String, Deque<Long>> attempts = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return LIMITED.stream().noneMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String key = clientIp(request) + " " + request.getRequestURI();
        long now = System.currentTimeMillis();

        // защита от роста карты при большом числе адресов
        if (attempts.size() > MAX_KEYS) {
            attempts.clear();
        }

        Deque<Long> window = attempts.computeIfAbsent(key, k -> new ArrayDeque<>());

        boolean blocked;
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > WINDOW_MS) {
                window.pollFirst();
            }
            blocked = window.size() >= MAX_ATTEMPTS;
            if (!blocked) {
                window.addLast(now);
            }
        }

        if (blocked) {
            log.warn("Слишком много попыток с {}", key);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Слишком много попыток, повторите через минуту\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Берём ПОСЛЕДНИЙ адрес из X-Forwarded-For: прокси (Caddy) добавляет реальный
     * IP клиента в конец, а всё, что пришло в заголовке от клиента, — подделка.
     * Первый элемент доверять нельзя, иначе лимит обходится одним заголовком.
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.lastIndexOf(',');
            return (comma >= 0 ? forwarded.substring(comma + 1) : forwarded).trim();
        }
        return request.getRemoteAddr();
    }
}
