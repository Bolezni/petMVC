package com.example.demo;

import com.example.demo.api.security.CustomUserDetails;
import com.example.demo.api.security.jwt.JwtUtil;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        String secret = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secret);
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 900_000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 2_592_000_000L);

        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("user@example.com")
                .password("x")
                .role(Roles.USER)
                .build();
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void accessToken_isMarkedAsAccess() {
        String token = jwtUtil.buildJwtToken(userDetails);

        assertThat(jwtUtil.isAccessToken(token)).isTrue();
        assertThat(jwtUtil.isRefreshToken(token)).isFalse();
        assertThat(jwtUtil.tokenType(token)).isEqualTo(JwtUtil.TYPE_ACCESS);
    }

    @Test
    void refreshToken_isMarkedAsRefresh() {
        String token = jwtUtil.buildRefreshToken(userDetails);

        assertThat(jwtUtil.isRefreshToken(token)).isTrue();
        assertThat(jwtUtil.isAccessToken(token)).isFalse();
        assertThat(jwtUtil.tokenType(token)).isEqualTo(JwtUtil.TYPE_REFRESH);
    }

    @Test
    void bothTokensCarrySameSubject() {
        assertThat(jwtUtil.usernameFromToken(jwtUtil.buildJwtToken(userDetails)))
                .isEqualTo("user@example.com");
        assertThat(jwtUtil.usernameFromToken(jwtUtil.buildRefreshToken(userDetails)))
                .isEqualTo("user@example.com");
    }
}
