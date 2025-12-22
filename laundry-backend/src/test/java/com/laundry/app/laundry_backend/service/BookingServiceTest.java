package com.laundry.app.laundry_backend.service;
import com.laundry.app.model.Booking;
import com.laundry.app.model.Machine;
import com.laundry.app.model.User;
import com.laundry.app.service.BookingService;
import com.laundry.app.repository.BookingRepository;
import com.laundry.app.repository.MachineRepository;
import com.laundry.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MachineRepository machineRepository;

    @InjectMocks
    private BookingService bookingService;

    private User mockUser;
    private Machine mockMachine;

    @BeforeEach
    void setUp() {
        // Setup common objects for tests
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setName("testuser");

        mockMachine = new Machine();
        mockMachine.setId(1L);
        mockMachine.setName("Washing Machine A");
    }

    @Test
    void createBooking_Success() {
        // Arrange
        Long userId = 1L;
        Long machineId = 1L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        // Mock repository behaviors
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(machineRepository.findById(machineId)).thenReturn(Optional.of(mockMachine));
        when(bookingRepository.existsOverlap(machineId, start, end)).thenReturn(false);

        // Mock the save method to return the booking object
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Booking result = bookingService.createBooking(userId, machineId, start, end);

        // Assert
        assertNotNull(result);
        assertEquals(mockUser, result.getUser());
        assertEquals(mockMachine, result.getMachine());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_DateValidationFailure() {
        // Arrange
        Long userId = 1L;
        Long machineId = 1L;
        // End time is BEFORE start time (invalid)
        LocalDateTime start = LocalDateTime.now().plusHours(2);
        LocalDateTime end = LocalDateTime.now().plusHours(1);

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.createBooking(userId, machineId, start, end);
        });

        assertEquals("La data di fine deve essere successiva alla data di inizio.", exception.getMessage());

        // Verify that repositories were never called
        verifyNoInteractions(userRepository);
        verifyNoInteractions(machineRepository);
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void createBooking_OverlapFailure() {
        // Arrange
        Long userId = 1L;
        Long machineId = 1L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(machineRepository.findById(machineId)).thenReturn(Optional.of(mockMachine));
        // Simulate an overlap exists
        when(bookingRepository.existsOverlap(machineId, start, end)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(userId, machineId, start, end);
        });

        assertEquals("La macchina è già prenotata in questo intervallo di tempo.", exception.getMessage());

        // Verify save was NEVER called
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_UserNotFound() {
        // Arrange
        Long userId = 99L; // Non-existent ID
        Long machineId = 1L;
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = LocalDateTime.now().plusHours(2);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookingService.createBooking(userId, machineId, start, end);
        });

        assertTrue(exception.getMessage().contains("Utente non trovato"));
    }
}