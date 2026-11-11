package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PetMvpApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void register_withValidData_returnsOk() throws Exception {
        String request = """
                {
                  "email": "userTest@example.com",
                  "password": "SecurePass123",
                  "passwordRepeat": "SecurePass123",
                  "phone": "+79991234591",
                  "name": "Dima",
                  "surname": "Yurchenko",
                  "gender": "MAN",
                  "birthday": "2005-04-07"
                }""";

        mockMvc.perform(post("/api/v1/auth/register")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void register_withInvalidEmail_returnsBadRequest() throws Exception {
        String request = """
                {
                  "email": "not-an-email",
                  "password": "SecurePass123",
                  "passwordRepeat": "SecurePass123",
                  "phone": "+79991234567",
                  "gender": "MAN"
                }""";

        mockMvc.perform(post("/api/v1/auth/register")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    void register_withMismatchedPasswords_returnsBadRequest() throws Exception {
        String request = """
                {
                  "email": "user2@example.com",
                  "password": "SecurePass123",
                  "passwordRepeat": "DifferentPass456",
                  "phone": "+79991234567",
                  "gender": "MAN"
                }""";

        mockMvc.perform(post("/api/v1/auth/register")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_afterRegister_returnsOk() throws Exception {
        String registerRequest = """
                {
                  "email": "logintest@example.com",
                  "password": "SecurePass123",
                  "passwordRepeat": "SecurePass123",
                  "phone": "+79991234568",
                  "gender": "WOMAN"
                }""";

        // сначала регистрируем пользователя — иначе логиниться будет некому
        mockMvc.perform(post("/api/v1/auth/register")
                        .content(registerRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String loginRequest = """
                {
                    "email": "logintest@example.com",
                    "password": "SecurePass123"
                }""";

        mockMvc.perform(post("/api/v1/auth/login")
                        .content(loginRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_withInvalidEmail_returnsBadRequest() throws Exception {
        String request = """
                {
                    "email": "s",
                    "password": "SecurePass123"
                }""";

        mockMvc.perform(post("/api/v1/auth/login")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());
    }
}
