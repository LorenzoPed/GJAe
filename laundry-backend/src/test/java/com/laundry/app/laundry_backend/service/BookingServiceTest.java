// java
package com.laundry.app.laundry_backend.service;

import com.laundry.app.dto.BookingRequest;
import com.laundry.app.model.*;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.MaintenanceRepository;
import com.laundry.app.repository.UserRepository;
import com.laundry.app.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private MachineRepository machineRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MaintenanceRepository maintenanceRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createBooking_ShouldReturnBooking_WhenValidRequest() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("mario");

        User mockUser = new User();
        mockUser.setUsername("mario");
        mockUser.setId(1L);

        Machine mockMachine = new Machine("Washer 1", MachineType.WASHER, true);
        mockMachine.setId(1L);

        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mockUser));
        when(machineRepository.findByTypeAndEnabledTrue(MachineType.WASHER)).thenReturn(List.of(mockMachine));

        when(maintenanceRepository.existsOverlap(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class), any(MaintenanceStatus.class)))
                .thenReturn(false);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingRequest request = new BookingRequest();
        request.setMachineType(MachineType.WASHER);
        request.setStartTime(LocalDateTime.now().plusHours(1));
        request.setEndTime(LocalDateTime.now().plusHours(2));

        Booking result = bookingService.createBooking(request);

        assertNotNull(result);
        assertEquals("Washer 1", result.getMachine().getName());
        assertEquals("mario", result.getUser().getUsername());
        assertEquals(BookingStatus.CONFIRMED, result.getStatus());

        verify(userRepository).findByUsername("mario");
        verify(bookingRepository).save(any(Booking.class));
        verify(machineRepository).findByTypeAndEnabledTrue(MachineType.WASHER);
    }
}
