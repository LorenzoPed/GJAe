package com.laundry.app.laundry_backend.service;

import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MaintenanceRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.service.MaintenanceService;
import com.laundry.app.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MaintenanceService focusing on input validation and delegation.
 */
@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    MaintenanceRepository maintenanceRepository;
    @Mock
    MachineRepository machineRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    NotificationService notificationService;

    @Test
    void scheduleMaintenance_throwsWhenMachineNotFound() {
        MaintenanceService svc = new MaintenanceService(maintenanceRepository, machineRepository, bookingRepository, notificationService);

        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(1);

        when(machineRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> svc.scheduleMaintenance(1L, start, end, "reason"));
        verify(machineRepository).findById(1L);
    }

    @Test
    void getUpcomingMaintenances_returnsEmptyForNullMachineId() {
        MaintenanceService svc = new MaintenanceService(maintenanceRepository, machineRepository, bookingRepository, notificationService);
        assertTrue(svc.getUpcomingMaintenances(null).isEmpty());
    }

    @Test
    void isMachineUnderMaintenanceNow_delegatesToRepository() {
        MaintenanceService svc = new MaintenanceService(maintenanceRepository, machineRepository, bookingRepository, notificationService);
        when(maintenanceRepository.existsActiveAt(eq(2L), any(), any())).thenReturn(true);

        assertTrue(svc.isMachineUnderMaintenanceNow(2L));
        verify(maintenanceRepository).existsActiveAt(eq(2L), any(), any());
    }
}
