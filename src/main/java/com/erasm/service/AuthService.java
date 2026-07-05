package com.erasm.service;

import com.erasm.dto.JwtResponse;
import com.erasm.dto.LoginRequest;
import com.erasm.dto.RegisterRequest;
import com.erasm.entity.Role;
import com.erasm.entity.User;
import com.erasm.exception.DuplicateResourceException;
import com.erasm.repository.RoleRepository;
import com.erasm.repository.UserRepository;
import com.erasm.security.CustomUserDetails;
import com.erasm.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil, AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.auditService = auditService;
    }

    public JwtResponse register(RegisterRequest request) {
        // Validation: Email must be unique
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("A user with email '" + request.getEmail() + "' already exists");
        }

        // Validation: Role mandatory
        Role role = roleRepository.findByRoleName(request.getRoleName().toUpperCase())
                .orElseThrow(() -> new IllegalArgumentException("Invalid role: " + request.getRoleName()));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        // Password minimum 8 characters is enforced by bean validation on the DTO
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setEnabled(true);
        user.setCreatedDate(LocalDateTime.now().toString());

        User saved = userRepository.save(user);
        logger.info("New user registered: {}", saved.getEmail());
        auditService.log("User", saved.getId(), "CREATE", "User registered with role " + role.getRoleName(),
                saved.getEmail());

        String token = jwtUtil.generateToken(saved.getEmail(), role.getRoleName());
        return new JwtResponse(token, saved.getEmail(), role.getRoleName());
    }

    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String role = userDetails.getUser().getRole().getRoleName();
        String token = jwtUtil.generateToken(userDetails.getUsername(), role);

        logger.info("User logged in: {}", userDetails.getUsername());
        auditService.log("User", userDetails.getUserId(), "LOGIN", "User logged in", userDetails.getUsername());

        return new JwtResponse(token, userDetails.getUsername(), role);
    }
}
