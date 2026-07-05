package com.erasm.config;

import com.erasm.entity.Role;
import com.erasm.entity.User;
import com.erasm.repository.RoleRepository;
import com.erasm.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the five mandatory roles and a default ADMIN user on first startup
 * so the API can be exercised in Postman immediately without manual SQL.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private static final List<String> DEFAULT_ROLES = List.of(
            "ADMIN", "DELIVERY_MANAGER", "RESOURCE_MANAGER", "EMPLOYEE", "AUDITOR");

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        DEFAULT_ROLES.forEach(roleName -> {
            if (roleRepository.findByRoleName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                logger.info("Seeded role: {}", roleName);
            }
        });

        if (!userRepository.existsByEmail("admin@erasm.com")) {
            Role adminRole = roleRepository.findByRoleName("ADMIN")
                    .orElseThrow(() -> new IllegalStateException("ADMIN role missing after seeding"));

            User admin = new User();
            admin.setName("System Administrator");
            admin.setEmail("admin@erasm.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setRole(adminRole);
            admin.setEnabled(true);
            admin.setCreatedDate(LocalDateTime.now().toString());
            userRepository.save(admin);
            logger.info("Seeded default admin user: admin@erasm.com / Admin@123");
        }
    }
}
