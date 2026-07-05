package com.erasm.service;

import com.erasm.dto.AssignRoleRequest;
import com.erasm.dto.ChangePasswordRequest;
import com.erasm.entity.Role;
import com.erasm.entity.User;
import com.erasm.exception.UserNotFoundException;
import com.erasm.repository.RoleRepository;
import com.erasm.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        Role role = new Role("EMPLOYEE");
        role.setId(1L);

        user = new User();
        user.setId(1L);
        user.setName("Jane Doe");
        user.setEmail("jane@erasm.com");
        user.setPassword("encoded-old-password");
        user.setRole(role);
    }

    @Test
    void changePassword_withCorrectOldPassword_succeeds() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("oldPass123");
        request.setNewPassword("newPass456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass123", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("newPass456")).thenReturn("encoded-new-password");

        assertDoesNotThrow(() -> userService.changePassword(1L, request));
    }

    @Test
    void changePassword_withIncorrectOldPassword_throwsException() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setOldPassword("wrongPassword");
        request.setNewPassword("newPass456");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encoded-old-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void assignRole_withValidRole_updatesUserRole() {
        Role newRole = new Role("RESOURCE_MANAGER");
        newRole.setId(2L);

        AssignRoleRequest request = new AssignRoleRequest();
        request.setRoleName("RESOURCE_MANAGER");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findByRoleName("RESOURCE_MANAGER")).thenReturn(Optional.of(newRole));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = userService.assignRole(1L, request);

        assertEquals("RESOURCE_MANAGER", response.getRoleName());
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(99L));
    }
}
