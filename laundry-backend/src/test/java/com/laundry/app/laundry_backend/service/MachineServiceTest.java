// java
package com.laundry.app.laundry_backend.service;

import com.laundry.app.model.Machine;
import com.laundry.app.model.MachineType;
import com.laundry.app.model.Booking;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.MaintenanceRepository;
import com.laundry.app.service.BookingService;
import com.laundry.app.service.MachineService;
import com.laundry.app.service.NotificationService;
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

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MaintenanceRepository maintenanceRepository;

    // Mock BookingService to avoid NPE when MachineService calls it
    @Mock
    private BookingService bookingService;

    // Mock NotificationService to avoid NPE when notifications are sent
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private MachineService machineService;

    private Machine mockMachine;

    @BeforeEach
    void setUp() {
        mockMachine = new Machine();
        mockMachine.setId(1L);
        mockMachine.setName("Washer A");
        mockMachine.setEnabled(true);
        mockMachine.setType(MachineType.WASHER);
    }

    @Test
    void getAllMachines_Success() {
        Machine machine2 = new Machine();
        machine2.setId(2L);
        machine2.setName("Dryer B");
        machine2.setEnabled(true);
        machine2.setType(MachineType.DRYER);

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
        assertEquals("Washer A", result.getName());
        assertEquals(MachineType.WASHER, result.getType());
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
        assertEquals(MachineType.WASHER, result.getType());
        verify(machineRepository).save(any(Machine.class));
    }

    @Test
    void updateMachine_Success() {
        Machine updatedDetails = new Machine();
        updatedDetails.setName("Washer A - Updated");
        updatedDetails.setEnabled(false);
        updatedDetails.setType(MachineType.DRYER);

        when(machineRepository.findById(1L)).thenReturn(Optional.of(mockMachine));
        when(machineRepository.save(any(Machine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Machine result = machineService.updateMachine(1L, updatedDetails);

        assertNotNull(result);
        assertEquals("Washer A - Updated", result.getName());
        assertFalse(result.isEnabled());
        assertEquals(MachineType.DRYER, result.getType());

        verify(machineRepository).save(any(Machine.class));
    }

    @Test
    void deleteMachine_Success() {
        when(machineRepository.existsById(1L)).thenReturn(true);

        // Stub bookingService.handleMachineDisabled to return a safe mock result
        BookingService.DisableMachineResult mockResult = mock(BookingService.DisableMachineResult.class);
        when(mockResult.getRescheduledBookings()).thenReturn(0);
        when(bookingService.handleMachineDisabled(1L)).thenReturn(mockResult);

        // Ensure bookingRepository returns an empty list (no remaining bookings)
        when(bookingRepository.findByMachineId(1L)).thenReturn(List.of());

        // Call the method under test
        machineService.deleteMachine(1L);

        verify(machineRepository, times(1)).deleteById(1L);
        verify(maintenanceRepository, times(1)).deleteByMachineId(1L);
        verify(bookingRepository, times(1)).findByMachineId(1L);
    }
}
