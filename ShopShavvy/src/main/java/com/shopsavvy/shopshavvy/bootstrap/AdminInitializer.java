package com.shopsavvy.shopshavvy.bootstrap;

import com.shopsavvy.shopshavvy.model.users.Role;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByEmail("manvi.jain1@tothenew.com")) {
            return;
        }

        Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN");
        if (adminRole == null) {
            throw new RuntimeException("ROLE_ADMIN must be initialized before creating the admin user.");
        }

        User adminUser = new User();
        adminUser.setEmail("manvi.jain1@tothenew.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword(passwordEncoder.encode("Admin123@"));
        adminUser.setIsActive(true);
        adminUser.addRole(adminRole);

        userRepository.save(adminUser);
        System.out.println("Admin user created successfully.");
    }
}
