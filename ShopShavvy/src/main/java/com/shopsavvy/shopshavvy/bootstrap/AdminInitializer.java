package com.shopsavvy.shopshavvy.bootstrap;

import com.shopsavvy.shopshavvy.model.users.Role;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Order(2)
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private MessageSource messageSource;

    private Locale getCurrentLocale() {
        return LocaleContextHolder.getLocale();
    }


    @Override
    public void run(String... args) throws Exception {
        if (userRepository.existsByEmail("manvi.jain1@tothenew.com")) {
            return;
        }

        Role adminRole = roleRepository.findByAuthority("ROLE_ADMIN");
        if (adminRole == null) {
            throw new RuntimeException(messageSource.getMessage("error.initialise.admin", null, getCurrentLocale()));
        }

        User adminUser = new User();
        adminUser.setEmail("manvi.jain1@tothenew.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPassword(passwordEncoder.encode("Admin123@"));
        adminUser.setIsActive(true);
        adminUser.addRole(adminRole);

        userRepository.save(adminUser);
        System.out.println(messageSource.getMessage("admin.register.success", null, getCurrentLocale()));
    }
}
