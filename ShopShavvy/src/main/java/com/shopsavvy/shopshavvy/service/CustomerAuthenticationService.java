package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.Exception.DuplicateEntryExistsException;
import com.shopsavvy.shopshavvy.Exception.EmailAlreadyExistsException;
import com.shopsavvy.shopshavvy.Exception.PasswordMismatchException;
import com.shopsavvy.shopshavvy.dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.AuthToken;
import com.shopsavvy.shopshavvy.model.users.Customer;
import com.shopsavvy.shopshavvy.model.users.TokenType;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.UserDetailsImpl;
import jakarta.mail.MessagingException;
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
    private final AuthTokenRepository authTokenRepository;

    public CustomerAuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtService jwtService,
            AuthTokenRepository authTokenRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
    }

    public User signup(CustomerRegistrationDTO customerRegistrationDTO) throws Exception {

        if(userRepository.existsByEmail(customerRegistrationDTO.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Customer customer = new Customer();
        customer.setEmail(customerRegistrationDTO.getEmail());
        customer.setFirstName(customerRegistrationDTO.getFirstName());
        customer.setLastName(customerRegistrationDTO.getLastName());
        customer.setPassword(passwordEncoder.encode(customerRegistrationDTO.getPassword()));
        customer.setContact(customerRegistrationDTO.getContact());

        if (customerRegistrationDTO.getMiddleName() != null && !customerRegistrationDTO.getMiddleName().isBlank()) {
            customer.setMiddleName(customerRegistrationDTO.getMiddleName());
        }

        if (!customerRegistrationDTO.getConfirmPassword().equals(customerRegistrationDTO.getPassword())) {
            throw new PasswordMismatchException("Confirm Password is not same as Password.");
        }

        userRepository.save(customer);

        UserDetailsImpl userDetails = new UserDetailsImpl(customer);
        String token = jwtService.generateToken(userDetails, "activation");

        AuthToken authToken = new AuthToken();
        authToken.setUserEmail(customer.getEmail());
        authToken.setToken(token);
        authToken.setTokenType(TokenType.ACTIVATION);
        authTokenRepository.save(authToken);


        try {
            emailService.sendActivationLink(customerRegistrationDTO, token);
        } catch (Exception e) {
            throw new MessagingException("Activation Link not send");
        }

        return customer;
    }
}