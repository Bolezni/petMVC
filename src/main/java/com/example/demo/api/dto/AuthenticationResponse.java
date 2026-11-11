package com.example.demo.api.dto;

public record AuthenticationResponse(
        String token,
        String refreshToken,
        String tokenType
) {
}
