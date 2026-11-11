package com.example.demo.api.service.authentication;

import com.example.demo.api.dto.AuthenticationRequest;
import com.example.demo.api.dto.AuthenticationResponse;
import com.example.demo.api.security.jwt.JwtUtil;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BaseAuthenticationService implements AuthenticationService {

    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public AuthenticationResponse authentication(AuthenticationRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.email(),
                request.password()
        ));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        String jwtToken = jwtUtil.buildJwtToken(userDetails);
        String refreshToken = jwtUtil.buildRefreshToken(userDetails);

        return new AuthenticationResponse(jwtToken, refreshToken, "Bearer");
    }

    @Override
    public AuthenticationResponse refresh(String refreshToken) {
        // подпись и срок проверяются при разборе токена
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new JwtException("Ожидается refresh-токен");
        }

        String email = jwtUtil.usernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new AuthenticationResponse(
                jwtUtil.buildJwtToken(userDetails),
                jwtUtil.buildRefreshToken(userDetails),
                "Bearer");
    }
}
