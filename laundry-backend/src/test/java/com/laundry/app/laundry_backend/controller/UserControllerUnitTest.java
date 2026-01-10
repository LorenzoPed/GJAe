// java
package com.laundry.app.laundry_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laundry.app.model.User;
import com.laundry.app.service.UserService;
import com.laundry.app.dto.UserMapper;
import com.laundry.app.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.laundry.app.controller.UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @Test
    void createUser_returnsCreatedDto() throws Exception {
        // Use a JSON map as request body (avoids constructing UserCreateRequest record directly)
        Map<String, Object> reqPayload = Map.of(
                "username", "unitUser",
                "email", "unit@example.com",
                "password", "secret"
        );

        User entity = new User();
        entity.setUsername("unitUser");
        entity.setEmail("unit@example.com");
        entity.setPassword("secret");

        User created = new User();
        created.setId(10L);
        created.setUsername("unitUser");
        created.setEmail("unit@example.com");

        // UserResponse is a record: construct via canonical constructor
        UserResponse resp = new UserResponse(10L, "unitUser", "unit@example.com");

        when(userMapper.toEntity(any())).thenReturn(entity);
        when(userService.createUser(any(User.class))).thenReturn(created);
        when(userMapper.toResponse(created)).thenReturn(resp);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqPayload)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("unitUser"))
                .andExpect(jsonPath("$.email").value("unit@example.com"));
    }

    @Test
    void getAllUsers_returnsArray() throws Exception {
        User u1 = new User(); u1.setId(1L); u1.setUsername("a"); u1.setEmail("a@x.com");
        User u2 = new User(); u2.setId(2L); u2.setUsername("b"); u2.setEmail("b@x.com");

        UserResponse r1 = new UserResponse(1L, "a", "a@x.com");
        UserResponse r2 = new UserResponse(2L, "b", "b@x.com");

        when(userService.getAllUsers()).thenReturn(List.of(u1, u2));
        when(userMapper.toResponse(u1)).thenReturn(r1);
        when(userMapper.toResponse(u2)).thenReturn(r2);

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserById_returnsUser_or404() throws Exception {
        User u = new User(); u.setId(5L); u.setUsername("x"); u.setEmail("x@x.com");
        UserResponse r = new UserResponse(5L, "x", "x@x.com");

        when(userService.getUser(5L)).thenReturn(u);
        when(userMapper.toResponse(u)).thenReturn(r);

        mockMvc.perform(get("/users/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("x"));

        when(userService.getUser(999L)).thenReturn(null);

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }
}
