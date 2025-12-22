
package com.laundry.app.laundry_backend.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.laundry.app.dto.UserCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Clean database after the test!
class UserControllerTest {

    @Autowired
    private com.laundry.app.repository.UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateNewUser() throws Exception {
        // 1. Creation of fake user
        UserCreateRequest newUser = new UserCreateRequest("Test User", "test@test.com");

        // 2. Simulation of POST request
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))

                // 3. Verifichiamo che tutto sia OK
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void shouldGetAllUsers() throws Exception {
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void shouldGetUserById() throws Exception {
        // 1. PREPARAZIONE: Salviamo manualmente un utente nel DB
        com.laundry.app.model.User savedUser = userRepository.save(new com.laundry.app.model.User("John Doe", "john@doe.com"));

        // 2. ESECUZIONE: Chiamiamo l'URL con l'ID appena creato
        mockMvc.perform(get("/users/" + savedUser.getId()) // es: /users/1
                        .contentType(MediaType.APPLICATION_JSON))

                // 3. VERIFICA
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Proviamo a cercare l'ID 999 che sicuramente non esiste (perché il test è @Transactional e parte pulito)
        mockMvc.perform(get("/users/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // Ci aspettiamo errore 404
    }
}