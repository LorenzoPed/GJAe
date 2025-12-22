package com.laundry.app.laundry_backend.service;

import com.laundry.app.model.Machine;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.service.MachineService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MachineServiceTest {

    @Mock
    private MachineRepository machineRepository;

    @InjectMocks
    private MachineService machineService;

    private Machine mockMachine;

    @BeforeEach
    void setUp() {
        mockMachine = new Machine();
        mockMachine.setId(1L);
        mockMachine.setName("Lavatrice A");
        mockMachine.setEnabled(true); // Impostiamo enabled a true
    }

    @Test
    void getAllMachines_Success() {
        Machine machine2 = new Machine();
        machine2.setId(2L);
        machine2.setName("Lavatrice B");
        machine2.setEnabled(false);

        when(machineRepository.findAll()).thenReturn(Arrays.asList(mockMachine, machine2));

        List<Machine> result = machineService.getAllMachines();

        assertEquals(2, result.size());
        verify(machineRepository, times(1)).findAll();
    }

    @Test
    void getMachineById_Success() {
        when(machineRepository.findById(1L)).thenReturn(Optional.of(mockMachine));

        Machine result = machineService.getMachineById(1L);

        assertNotNull(result);
        assertEquals("Lavatrice A", result.getName());
        assertTrue(result.isEnabled()); // Verifichiamo che sia enabled
    }

    @Test
    void getMachineById_NotFound() {
        when(machineRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            machineService.getMachineById(99L);
        });

        assertTrue(exception.getMessage().contains("not found"));
    }

    @Test
    void createMachine_Success() {
        when(machineRepository.save(any(Machine.class))).thenReturn(mockMachine);

        Machine result = machineService.createMachine(mockMachine);

        assertNotNull(result);
        assertEquals("Lavatrice A", result.getName());
        verify(machineRepository).save(any(Machine.class));
    }

    @Test
    void updateMachine_Success() {
        // Arrange
        Machine updatedDetails = new Machine();
        updatedDetails.setName("Lavatrice A - Updated");

        updatedDetails.setEnabled(false);

        when(machineRepository.findById(1L)).thenReturn(Optional.of(mockMachine));
        when(machineRepository.save(any(Machine.class))).thenReturn(updatedDetails);

        // Act
        Machine result = machineService.updateMachine(1L, updatedDetails);

        // Assert
        assertNotNull(result);
        verify(machineRepository).save(any(Machine.class));
    }

    @Test
    void deleteMachine_Success() {
        when(machineRepository.existsById(1L)).thenReturn(true);

        machineService.deleteMachine(1L);

        verify(machineRepository, times(1)).deleteById(1L);
    }
}