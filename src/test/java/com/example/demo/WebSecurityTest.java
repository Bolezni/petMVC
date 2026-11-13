package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Страницы без токена уводят на вход, а не отдают JSON.
 * API на том же запросе отвечает 401 — это проверяется отдельно.
 */
@SpringBootTest
@AutoConfigureMockMvc
class WebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void appPage_redirectsAnonymousToLogin() throws Exception {
        mockMvc.perform(get("/app"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void specialistPage_redirectsAnonymousToLogin() throws Exception {
        mockMvc.perform(get("/specialist"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void api_returns401InsteadOfRedirect() throws Exception {
        mockMvc.perform(get("/api/v1/user/programs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void landingPage_isPublic() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
