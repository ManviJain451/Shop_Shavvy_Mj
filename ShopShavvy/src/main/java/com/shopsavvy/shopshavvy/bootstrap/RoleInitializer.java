package com.shopsavvy.shopshavvy.bootstrap;

import com.shopsavvy.shopshavvy.model.users.Role;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
public class RoleInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    public RoleInitializer(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        List<String> roles = Arrays.asList("ROLE_ADMIN", "ROLE_CUSTOMER", "ROLE_SELLER");

        for (String authority : roles) {
            Role role = roleRepository.findByAuthority(authority);
            if (role == null) {
                role = new Role();
                role.setAuthority(authority);
                roleRepository.save(role);
            }
        }
    }
}
