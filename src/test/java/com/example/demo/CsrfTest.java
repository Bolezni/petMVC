package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * CSRF включён: страницы отдаются сервером, вход по cookie.
 * Формы получают скрытое поле от Thymeleaf, запрос без токена отклоняется.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CsrfTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginForm_containsCsrfField() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void loginWithoutToken_isRejected() throws Exception {
        mockMvc.perform(post("/login")
                        .param("email", "demo@petmvp.local")
                        .param("password", "DemoPass123"))
                .andExpect(status().isForbidden());
    }

    @Test
    void loginWithToken_isAccepted() throws Exception {
        // валидный токен проходит CSRF-проверку; неверные данные уводят на форму с ошибкой
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", "nobody@example.com")
                        .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    void apiWithBearerToken_doesNotNeedCsrf() throws Exception {
        // без токена CSRF запрос доходит до авторизации и получает 401, а не 403
        mockMvc.perform(post("/api/v1/user/questionnaire")
                        .header("Authorization", "Bearer invalid")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
