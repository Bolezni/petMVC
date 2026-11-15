package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Регрессия: строки формы программы стали объектами, и имя поля однажды
 * превратилось в "exerciseIdProgramRow[index=1, ...]" — форма уходила пустой.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpecialistProgramFormTest {

    @Autowired
    private MockMvc mockMvc;

    private static final SimpleGrantedAuthority SPECIALIST = new SimpleGrantedAuthority("SPECIALIST");

    @Test
    void newProgramForm_usesNumericFieldNames() throws Exception {
        mockMvc.perform(get("/specialist/users/1/programs/new")
                        .with(user("spec@test.local").authorities(SPECIALIST)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"exerciseId1\"")))
                .andExpect(content().string(containsString("name=\"note1\"")))
                .andExpect(content().string(not(containsString("exerciseIdProgramRow"))));
    }

    @Test
    void newProgramForm_prefillsNoteWithSlow() throws Exception {
        mockMvc.perform(get("/specialist/users/1/programs/new")
                        .with(user("spec@test.local").authorities(SPECIALIST)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Медленно\"")));
    }

    /**
     * Регрессия: навигация и форма выхода зависели от #authentication из extras-библиотеки,
     * которой нет, поэтому блок молча не рендерился и выйти из аккаунта было нельзя.
     */
    @Test
    void header_showsNavigationAndLogoutForAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/specialist/users/1/programs/new")
                        .with(user("spec@test.local").authorities(SPECIALIST)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Выйти")))
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(content().string(containsString("/specialist/exercises")));
    }

    @Test
    void header_hidesNavigationForAnonymousUser() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Выйти"))));
    }
}
