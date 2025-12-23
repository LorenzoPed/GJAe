package com.laundry.app.laundry_backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.model.Role;
import com.laundry.app.model.User;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // This rolls back DB changes after the test finishes
class BookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MachineRepository machineRepository;

    @Autowired
    private ObjectMapper objectMapper; // Converts Objects to JSON strings

    @Test
    @WithMockUser(username = "testuser") // Bypasses JWT login, simulating a logged-in user
    void createBooking_EndToEnd_ShouldReturn200() throws Exception {

        // 1. SETUP DATA IN REAL DB (H2)
        // We need a real user in DB because the Service looks it up
        User user = new User();
        user.setUsername("testuser");
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setRole(Role.USER);
        userRepository.save(user);

        // We need a real machine in DB
        Machine machine = new Machine("Integration Washer", MachineType.WASHER, true);
        machine = machineRepository.save(machine);

        // 2. PREPARE THE REQUEST
        BookingRequest request = new BookingRequest();
        request.setMachineId(machine.getId());
        request.setStartTime(LocalDateTime.now().plusDays(1));
        request.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));

        // 3. PERFORM POST REQUEST
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))) // Convert object to JSON

                // 4. VERIFY RESPONSE
                .andExpect(status().isOk()) // Expect HTTP 200
                .andExpect(jsonPath("$.id").exists()) // Expect an ID in response
                .andExpect(jsonPath("$.status").value("CONFIRMED")); // Expect status CONFIRMED
    }

    @Test
    @WithAnonymousUser // Forces the user to be unauthenticated for this specific test
    void shouldRejectAnonymousAccess() throws Exception {
        // Attempt to access a secured endpoint without authentication
        // Expecting HTTP 401 Unauthorized
        mockMvc.perform(get("/api/secured-resource"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    // CASE 2: 403 Forbidden
    // We simulate a user with a "USER" role trying to access an "ADMIN" area
    @WithMockUser(username = "basicUser", roles = {"USER"})
    void shouldReturn403WhenAccessingAdminEndpoint() throws Exception {
        // Attempt to access an endpoint that requires ADMIN role
        // This assumes your SecurityConfig restricts "/api/admin/**" to ADMINs
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }
}
