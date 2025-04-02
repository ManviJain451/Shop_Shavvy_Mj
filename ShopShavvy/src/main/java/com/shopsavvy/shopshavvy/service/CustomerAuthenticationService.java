package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CustomerAuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;

    public CustomerAuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
    }

    public User signup(CustomerRegistrationDTO customerRegistrationDTO) throws Exception {
        Customer customer = new Customer();
        customer.setEmail(customerRegistrationDTO.getEmail());
        customer.setFirstName(customerRegistrationDTO.getFirstName());
        customer.setLastName(customerRegistrationDTO.getLastName());
        customer.setPassword(passwordEncoder.encode(customerRegistrationDTO.getPassword()));
        customer.setIsActive(false);
        customer.setExpired(false);
        customer.setLocked(false);
        customer.setCredentialsExpired(false);
        customer.setDateCreated(LocalDateTime.now());
        customer.setLastUpdated(LocalDateTime.now());
        customer.setContact(customerRegistrationDTO.getContact());

        if(customerRegistrationDTO.getConfirmPassword() != customerRegistrationDTO.getPassword()){
            throw  new Exception("Confirm Password is not same as Password.");
        }

        userRepository.save(customer);

        UserDetailsImpl userDetails = new UserDetailsImpl(customer);
        String token = jwtService.generateToken(userDetails, "activation");

        try {
            emailService.sendActivationLink(customerRegistrationDTO, token);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return customer;
    }
}