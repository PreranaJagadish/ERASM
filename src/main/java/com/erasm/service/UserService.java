package com.erasm.service;

import com.erasm.dto.AssignRoleRequest;
import com.erasm.dto.ChangePasswordRequest;
import com.erasm.dto.UserResponse;
import com.erasm.dto.UserUpdateRequest;
import com.erasm.entity.Role;
import com.erasm.entity.User;
import com.erasm.exception.DuplicateResourceException;
import com.erasm.exception.UserNotFoundException;
import com.erasm.repository.RoleRepository;
import com.erasm.repository.UserRepository;
import com.erasm.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return toResponse(user);
    }

    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User user = findUserOrThrow(id);

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        user.setModifiedDate(LocalDateTime.now().toString());

        User saved = userRepository.save(user);
        logger.info("User updated: {}", saved.getEmail());
        auditService.log("User", saved.getId(), "UPDATE", "User details updated", SecurityUtil.getCurrentUserEmail());
        return toResponse(saved);
    }

    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
        logger.info("User deleted: {}", user.getEmail());
        auditService.log("User", id, "DELETE", "User deleted", SecurityUtil.getCurrentUserEmail());
    }

    public void changePassword(Long id, ChangePasswordRequest request) {
        User user = findUserOrThrow(id);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("Old password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setModifiedDate(LocalDateTime.now().toString());
        userRepository.save(user);
        logger.info("Password changed for user: {}", user.getEmail());
        auditService.log("User", user.getId(), "UPDATE", "Password changed", SecurityUtil.getCurrentUserEmail());
    }

    public UserResponse assignRole(Long id, AssignRoleRequest request) {
        User user = findUserOrThrow(id);

        Role role = roleRepository.findByRoleName(request.getRoleName().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.getRoleName()));

        user.setRole(role);
        user.setModifiedDate(LocalDateTime.now().toString());
        User saved = userRepository.save(user);

        logger.info("Role '{}' assigned to user {}", role.getRoleName(), saved.getEmail());
        auditService.log("User", saved.getId(), "UPDATE", "Role changed to " + role.getRoleName(),
                SecurityUtil.getCurrentUserEmail());

        return toResponse(saved);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole() != null ? user.getRole().getRoleName() : null, user.isEnabled());
    }
}
