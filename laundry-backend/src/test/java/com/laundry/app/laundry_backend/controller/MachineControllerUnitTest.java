package com.laundry.app.laundry_backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.service.MachineService;
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

@WebMvcTest(controllers = com.laundry.app.controller.MachineController.class)
@AutoConfigureMockMvc(addFilters = false)
class MachineControllerUnitTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MachineService machineService;

    @Test
    void createMachine_returnsCreatedMachine() throws Exception {
        Map<String, Object> payload = Map.of(
                "name", "Unit Washer",
                "enabled", true,
                "type", "WASHER"
        );

        Machine returned = new Machine();
        returned.setId(1L);
        returned.setName("Unit Washer");
        returned.setEnabled(true);
        returned.setType(MachineType.WASHER);

        when(machineService.createMachine(any())).thenReturn(returned);

        mockMvc.perform(post("/api/machines")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Unit Washer"))
                .andExpect(jsonPath("$.type").value("WASHER"));
    }

    @Test
    void getAllMachines_returnsArray() throws Exception {
        Machine m1 = new Machine();
        m1.setId(1L);
        m1.setName("A");
        m1.setEnabled(true);
        m1.setType(MachineType.WASHER);
        Machine m2 = new Machine();
        m2.setId(2L);
        m2.setName("B");
        m2.setEnabled(true);
        m2.setType(MachineType.DRYER);

        when(machineService.getAllMachines()).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/machines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getMachineById_returns200_or404() throws Exception {
        Machine m = new Machine();
        m.setId(5L);
        m.setName("Single");
        m.setEnabled(true);
        m.setType(MachineType.WASHER);

        when(machineService.getMachineById(5L)).thenReturn(m);
        when(machineService.getMachineById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/machines/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.name").value("Single"));

        mockMvc.perform(get("/api/machines/999"))
                .andExpect(status().isNotFound());
    }
}