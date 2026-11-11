package com.example.demo;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthRefreshFlowTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String EMAIL = "refresh-user@example.com";
    private static final String PHONE = "+79991230001";

    private void register() throws Exception {
        String request = """
                {
                  "email": "%s",
                  "password": "SecurePass123",
                  "passwordRepeat": "SecurePass123",
                  "phone": "%s",
                  "gender": "MAN"
                }""".formatted(EMAIL, PHONE);

        mockMvc.perform(post("/api/v1/auth/register")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private String login() throws Exception {
        String request = """
                {
                  "email": "%s",
                  "password": "SecurePass123"
                }""".formatted(EMAIL);

        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .content(request)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return body;
    }

    @Test
    void refresh_returnsNewAccessToken() throws Exception {
        register();
        String refreshToken = JsonPath.read(login(), "$.refreshToken");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void refresh_withAccessToken_returnsUnauthorized() throws Exception {
        register();
        String accessToken = JsonPath.read(login(), "$.token");

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .content("{\"refreshToken\":\"" + accessToken + "\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refreshToken_isNotAcceptedAsBearer() throws Exception {
        register();
        String refreshToken = JsonPath.read(login(), "$.refreshToken");

        mockMvc.perform(get("/api/v1/user/questionnaire")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void questionnaire_withoutQuestionnaire_returnsNotFound() throws Exception {
        register();
        String accessToken = JsonPath.read(login(), "$.token");

        assertThat(accessToken).isNotBlank();

        mockMvc.perform(get("/api/v1/user/questionnaire")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }
}
