package com.laundry.app.laundry_backend.service;

import com.laundry.app.model.User;
import com.laundry.app.repository.UserRepository;
import com.laundry.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserService: uniqueness checks and password encoding.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    void createUser_throwsWhenUsernameExists() {
        User u = mock(User.class);
        when(u.getUsername()).thenReturn("bob");
        when(userRepository.existsByUsername("bob")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> userService.createUser(u));
        verify(userRepository).existsByUsername("bob");
    }

    @Test
    void createUser_encodesPasswordAndSaves() {
        User u = mock(User.class);
        when(u.getUsername()).thenReturn("alice");
        when(u.getEmail()).thenReturn("a@example.com");
        when(u.getPassword()).thenReturn("plain");
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(userRepository.existsByEmail("a@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(u)).thenReturn(u);

        User saved = userService.createUser(u);

        verify(u).setPassword("encoded");
        verify(userRepository).save(u);
        assertSame(u, saved);
    }
}
