package com.erasm.service;

import com.erasm.dto.RegisterRequest;
import com.erasm.entity.Role;
import com.erasm.entity.User;
import com.erasm.exception.DuplicateResourceException;
import com.erasm.repository.RoleRepository;
import com.erasm.repository.UserRepository;
import com.erasm.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_withDuplicateEmail_throwsException() {
        RegisterRequest request = new RegisterRequest();
        request.setName("John Doe");
        request.setEmail("john@erasm.com");
        request.setPassword("password123");
        request.setRoleName("EMPLOYEE");

        when(userRepository.existsByEmail("john@erasm.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    void register_withNewEmail_encodesPasswordAndReturnsToken() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Jane Doe");
        request.setEmail("jane@erasm.com");
        request.setPassword("password123");
        request.setRoleName("EMPLOYEE");

        Role role = new Role("EMPLOYEE");
        role.setId(1L);

        when(userRepository.existsByEmail("jane@erasm.com")).thenReturn(false);
        when(roleRepository.findByRoleName("EMPLOYEE")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("dummy-jwt-token");

        var response = authService.register(request);

        assertEquals("dummy-jwt-token", response.getToken());
        assertEquals("jane@erasm.com", response.getEmail());
        assertEquals("EMPLOYEE", response.getRole());
    }
}
