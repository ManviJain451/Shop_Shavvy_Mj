package com.shopsavvy.shopshavvy.bootstrap;

import com.shopsavvy.shopshavvy.model.user.Role;
import com.shopsavvy.shopshavvy.model.user.User;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Value("${admin.first.name}")
    private String adminFirstName;

    @Value("${admin.last.name}")
    private String adminLastName;

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN");
        if (adminRole == null) {
            throw new RuntimeException("ROLE_ADMIN must be initialized before creating the admin user.");
        }

        User adminUser = new User();
        adminUser.setEmail(adminEmail);
        adminUser.setFirstName(adminFirstName);
        adminUser.setLastName(adminLastName);
        adminUser.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
        adminUser.setIsActive(true);
        adminUser.addRole(adminRole);

        userRepository.save(adminUser);
        log.info("Admin is registered successfully.");
    }
}
