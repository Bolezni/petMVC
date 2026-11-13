package com.example.demo.api.security.jwt;

import com.example.demo.api.security.AuthCookies;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    /** Токен для браузера: кладём в httpOnly-cookie, чтобы страницы работали без JS. */
    public static final String ACCESS_COOKIE = "access_token";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AuthCookies authCookies;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/v1/auth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token != null) {
            try {
                authenticate(token, request);
            } catch (Exception ex) {
                log.debug("Access-токен не принят: {}", ex.getMessage());
                SecurityContextHolder.clearContext();

                // протух access — пробуем обновить его по refresh-cookie без разлогина
                refreshSilently(request, response);
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String token, HttpServletRequest request) {
        if (!jwtUtil.isAccessToken(token)) {
            throw new IllegalStateException("Ожидался access-токен");
        }

        String email = jwtUtil.usernameFromToken(token);
        if (email == null) {
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtUtil.isTokenValidate(token, userDetails)) {
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void refreshSilently(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookie(request, AuthCookies.REFRESH_COOKIE);
        if (refreshToken == null) {
            return;
        }

        try {
            if (!jwtUtil.isRefreshToken(refreshToken)) {
                return;
            }

            String email = jwtUtil.usernameFromToken(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            String newAccessToken = jwtUtil.buildJwtToken(userDetails);
            authCookies.writeAccess(response, newAccessToken);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Access-токен обновлён по refresh-cookie");
        } catch (Exception ex) {
            log.debug("Обновить токен не удалось: {}", ex.getMessage());
        }
    }

    /**
     * API-клиенты присылают заголовок Authorization, браузер — cookie.
     */
    private String resolveToken(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return readCookie(request, ACCESS_COOKIE);
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
