package com.linguanotes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotesFlowTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void notesAreScopedToTheirOwner() throws Exception {
        MockHttpSession owner = registerAndLogin("owner");
        MockHttpSession other = registerAndLogin("other");

        MvcResult created = mvc.perform(post("/api/notes")
                        .session(owner)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Ma note","content":"# Bonjour","type":"VOCABULARY",
                                 "unit":"Unité 1","tags":["salutations"],"favorite":false}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Ma note"))
                .andExpect(jsonPath("$.tags[0]").value("salutations"))
                .andReturn();
        long noteId = json.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/api/notes").session(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mvc.perform(put("/api/notes/" + noteId)
                        .session(owner)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Note modifiée","content":"# Salut","type":"GRAMMAR","tags":[]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Note modifiée"));

        mvc.perform(patch("/api/notes/" + noteId + "/favorite").session(owner).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorite").value(true));

        mvc.perform(get("/api/notes/" + noteId).session(other))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/notes/" + noteId).session(owner).with(csrf()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/notes/" + noteId).session(owner))
                .andExpect(status().isNotFound());
    }

    private MockHttpSession registerAndLogin(String prefix) throws Exception {
        String email = prefix + "-" + UUID.randomUUID() + "@example.com";
        String password = "motdepasse123";
        mvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","email":"%s","password":"%s"}
                                """.formatted(prefix, email, password)))
                .andExpect(status().isCreated());
        MvcResult login = mvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) login.getRequest().getSession(false);
    }
}
