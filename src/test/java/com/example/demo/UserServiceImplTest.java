package com.example.demo;

import com.example.demo.api.dto.AuthenticationRequest;
import com.example.demo.api.dto.AuthenticationResponse;
import com.example.demo.api.dto.RegisterRequest;
import com.example.demo.api.security.CheckAuthUtil;
import com.example.demo.api.security.CustomUserDetails;
import com.example.demo.api.security.jwt.JwtUtil;
import com.example.demo.api.service.authentication.BaseAuthenticationService;
import com.example.demo.api.service.authentication.BaseRegisterService;
import com.example.demo.store.entities.Gender;
import com.example.demo.store.entities.UserEntity;
import com.example.demo.store.repo.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private BaseRegisterService registerService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @InjectMocks
    private BaseAuthenticationService authenticationService;

    private MockedStatic<CheckAuthUtil> checkAuthUtilMock;

    @BeforeEach
    void setUp() {
        checkAuthUtilMock = Mockito.mockStatic(CheckAuthUtil.class);
    }

    @AfterEach
    void tearDown() {
        checkAuthUtilMock.close();
    }

    @Test
    void createUser() {
        RegisterRequest request = new RegisterRequest(
                "test@mail.ru", "123456", "123456",
                "89101714588", "Dima", null, Gender.MAN, LocalDate.of(2005, 4, 7));

        when(passwordEncoder.encode("123456")).thenReturn("encoded_123456");

        registerService.register(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(repository).saveAndFlush(captor.capture());

        UserEntity saved = captor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("test@mail.ru");
        assertThat(saved.getName()).isEqualTo("Dima");
        assertThat(saved.getSurname()).isEqualTo("");
        assertThat(saved.getPhone()).isEqualTo("89101714588");
        assertThat(saved.getPassword()).isEqualTo("encoded_123456");
        assertThat(saved.getGender()).isEqualTo(Gender.MAN);
        assertThat(saved.getBirthday()).isEqualTo(LocalDate.of(2005, 4, 7));
    }

    @Test
    void authUser() {
        AuthenticationRequest request = new AuthenticationRequest("userTest@example.com", "SecurePass123");

        UserEntity currentUser = UserEntity.builder()
                .id(1L)
                .email("userTest@example.com")
                .build();
        CustomUserDetails userDetails = new CustomUserDetails(currentUser);

        when(userDetailsService.loadUserByUsername("userTest@example.com"))
                .thenReturn(userDetails);
        when(jwtUtil.buildJwtToken(userDetails)).thenReturn("mocked-jwt-token");
        when(jwtUtil.buildRefreshToken(userDetails)).thenReturn("mocked-refresh-token");

        AuthenticationResponse response = authenticationService.authentication(request);

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("userTest@example.com", "SecurePass123"));

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("mocked-jwt-token");
        assertThat(response.refreshToken()).isEqualTo("mocked-refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }
}
