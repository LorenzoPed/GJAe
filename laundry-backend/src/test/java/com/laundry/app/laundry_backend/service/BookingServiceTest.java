//package com.laundry.app.laundry_backend.service;
//
//import com.laundry.app.dto.BookingRequest;
//import com.laundry.app.model.*;
//import com.laundry.app.repository.BookingRepository;
//import com.laundry.app.repository.MachineRepository;
//import com.laundry.app.repository.UserRepository;
//import com.laundry.app.service.BookingService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class BookingServiceTest {
//
//    @Mock
//    private BookingRepository bookingRepository;
//
//    @Mock
//    private MachineRepository machineRepository;
//
//    @Mock
//    private UserRepository userRepository;
//
//    @Mock
//    private SecurityContext securityContext;
//
//    @Mock
//    private Authentication authentication;
//
//    @InjectMocks
//    private BookingService bookingService;
//
//    @BeforeEach
//    void setUp() {
//        SecurityContextHolder.setContext(securityContext);
//    }
//
//    @Test
//    void createBooking_ShouldReturnBooking_WhenValidRequest() {
//
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        when(authentication.getName()).thenReturn("mario");
//
//        User mockUser = new User();
//        mockUser.setUsername("mario");
//        mockUser.setId(1L);
//
//        Machine mockMachine = new Machine("Washer 1", MachineType.WASHER, true);
//        mockMachine.setId(1L); // We force the ID for the test
//
//        when(userRepository.findByUsername("mario")).thenReturn(Optional.of(mockUser));
//        when(machineRepository.findById(1L)).thenReturn(Optional.of(mockMachine));
//
//        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
//            Booking b = invocation.getArgument(0);
//
//            return b;
//        });
//
//        BookingRequest request = new BookingRequest();
//        request.setMachineId(1L);
//        request.setStartTime(LocalDateTime.now().plusHours(1));
//        request.setEndTime(LocalDateTime.now().plusHours(2));
//
//        Booking result = bookingService.createBooking(request);
//
//        assertNotNull(result);
//        assertEquals("Washer 1", result.getMachine().getName());
//        assertEquals("mario", result.getUser().getUsername());
//        assertEquals(BookingStatus.CONFIRMED, result.getStatus());
//
//        verify(userRepository).findByUsername("mario");
//        verify(bookingRepository).save(any(Booking.class));
//    }
//}