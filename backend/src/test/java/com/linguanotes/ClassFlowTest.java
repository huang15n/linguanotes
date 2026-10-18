package com.linguanotes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linguanotes.model.AccountRole;
import com.linguanotes.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClassFlowTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Autowired
    private UserAccountRepository accounts;

    @Test
    void teacherCreatesClassAndStudentJoinsWithCode() throws Exception {
        String teacherEmail = "teacher-" + UUID.randomUUID() + "@example.com";
        String studentEmail = "student-" + UUID.randomUUID() + "@example.com";
        MockHttpSession teacher = registerAndLogin("Prof", teacherEmail);
        accounts.findByEmail(teacherEmail).ifPresent(account -> {
            account.setRole(AccountRole.TEACHER);
            accounts.save(account);
        });
        // Re-login so the session carries the TEACHER authority.
        teacher = login(teacherEmail);

        MvcResult created = mvc.perform(post("/api/classes")
                        .session(teacher)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Espagnol A1","description":"Classe de test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Espagnol A1"))
                .andReturn();
        JsonNode classJson = json.readTree(created.getResponse().getContentAsString());
        String joinCode = classJson.get("joinCode").asText();

        MockHttpSession student = registerAndLogin("Élève", studentEmail);
        mvc.perform(post("/api/classes/join")
                        .session(student)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"joinCode\":\"" + joinCode + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.owner").value(false));

        mvc.perform(get("/api/classes/" + classJson.get("id").asLong() + "/members")
                        .session(student))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value(studentEmail));
    }

    private MockHttpSession registerAndLogin(String name, String email) throws Exception {
        String password = "motdepasse123";
        mvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"%s"}
                                """.formatted(name, email, password)))
                .andExpect(status().isCreated());
        return login(email);
    }

    private MockHttpSession login(String email) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"motdepasse123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
