package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.exception.*;
import com.shopsavvy.shopshavvy.dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.token.BlackListedToken;
import com.shopsavvy.shopshavvy.model.users.*;
import com.shopsavvy.shopshavvy.repository.BlackListedTokenRepository;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import javax.management.relation.RoleNotFoundException;
import java.util.Date;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class CustomerAuthenticationService {

    @Value("${jwt.expiration-time.accessToken}")
    private long accessTokenExpirationTime;

    @Value("${jwt.expiration-time.refreshToken}")
    private long refreshTokenExpirationTime;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final AuthenticationService authenticationService;
    private final RoleRepository roleRepository;
    private final BlackListedTokenRepository blackListedTokenRepository;
    private final BlackListedTokenService blackListedTokenService;


    public String registerCustomer(CustomerRegistrationDTO customerRegistrationDTO) throws Exception {

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

        Role role = roleRepository.findByAuthority("ROLE_CUSTOMER");
        if(role != null){
            customer.addRole(role);
        }

        userRepository.save(customer);

        UserDetailsImpl userDetails = new UserDetailsImpl(customer);
        String token = jwtService.generateToken(userDetails, "activation");
        Claims claims = jwtService.extractAllClaims(token);
        AuthToken authToken = new AuthToken();
        authToken.setUserEmail(customer.getEmail());
        authToken.setToken(token);
        authToken.setTokenType("activation");
        authToken.setExpirationTime(claims.getExpiration());
        authTokenRepository.save(authToken);


        try {
            emailService.sendActivationLink(customerRegistrationDTO.getEmail(), token);
        } catch (Exception e) {
            throw new Exception("Mail for activating the account is not send");
        }


        return "Customer has been registered";
    }


    @Transactional
    public String activateCustomer(@RequestParam String token) throws Exception {
            String userEmail = jwtService.extractUsername(token);
            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

            if (user.getIsActive()) {
                throw new AlreadyActivatedException("User is already activated");
            }

            UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);

            if (jwtService.isTokenValid(token, userDetailsImpl, "activation")) {

                user.setIsActive(true);
                userRepository.save(user);
                verifyCustomer(user.getEmail());

            }
        return "User is activated.";

    }

    public ResponseEntity<Void> verifyCustomer(String email) throws Exception{
        try {
            emailService.sendVerificationEmail(email, "Account Activated", "Your account has been successfully activated.");
        } catch (Exception e) {
            throw new Exception("Verification Email is not send");
        }
        return ResponseEntity.ok().build();
    }

    @Transactional
    public ResponseEntity<String> resendActivationLink(@RequestParam String email) throws Exception {
        if (!userRepository.existsByEmail(email)) {
            throw new UserNotFoundException("User not found");
        }

        Role role = roleRepository.findByAuthority("ROLE_CUSTOMER");
        if(role== null){
            throw new RoleNotFoundException("Role not found");
        }

        User user = userRepository.findByEmailAndRoles(email, Set.of(role))
                .orElseThrow(() -> new UserNotFoundException("Customer not found with this email"));

        if (user.getIsActive()) {
            throw new AlreadyActivatedException("User is already activated.");
        }

        authTokenRepository.deleteActivationTokenByEmail(email);

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String token = jwtService.generateToken(userDetails, "activation");

        Claims claims = jwtService.extractAllClaims(token);

        AuthToken authToken = new AuthToken();
        authToken.setUserEmail(email);
        authToken.setToken(token);
        authToken.setTokenType("activation");
        authToken.setExpirationTime(claims.getExpiration());
        authTokenRepository.save(authToken);

        try {
            emailService.sendActivationLink(email, token);
        } catch (Exception e) {
            throw new Exception("Mail for activating the account is not send");
        }

        return ResponseEntity.ok().body("The activation link is sent to registered email.");
    }



}