package com.example.demo;

import com.example.demo.api.dto.AuthenticationResponse;
import com.example.demo.api.security.CustomUserDetails;
import com.example.demo.api.security.jwt.JwtUtil;
import com.example.demo.api.service.authentication.BaseAuthenticationService;
import com.example.demo.store.entities.Roles;
import com.example.demo.store.entities.UserEntity;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private BaseAuthenticationService service;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .email("user@example.com")
                .role(Roles.USER)
                .build();
        userDetails = new CustomUserDetails(user);
    }

    @Test
    void refresh_rejectsAccessToken() {
        when(jwtUtil.isRefreshToken("access-token")).thenReturn(false);

        assertThatThrownBy(() -> service.refresh("access-token"))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void refresh_issuesNewPair() {
        when(jwtUtil.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtUtil.usernameFromToken("refresh-token")).thenReturn("user@example.com");
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(jwtUtil.buildJwtToken(userDetails)).thenReturn("new-access");
        when(jwtUtil.buildRefreshToken(userDetails)).thenReturn("new-refresh");

        AuthenticationResponse response = service.refresh("refresh-token");

        assertThat(response.token()).isEqualTo("new-access");
        assertThat(response.refreshToken()).isEqualTo("new-refresh");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
