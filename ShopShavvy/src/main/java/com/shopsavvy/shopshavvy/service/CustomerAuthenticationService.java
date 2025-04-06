package com.shopsavvy.shopshavvy.service;

import com.shopsavvy.shopshavvy.Exception.*;
import com.shopsavvy.shopshavvy.dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.token.AuthToken;
import com.shopsavvy.shopshavvy.model.token.TokenType;
import com.shopsavvy.shopshavvy.model.users.*;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.RoleRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.securityConfigurations.UserDetailsImpl;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@Service
public class CustomerAuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthTokenRepository authTokenRepository;
    private final AuthenticationService authenticationService;
    private final RoleRepository roleRepository;

    public CustomerAuthenticationService(
            UserRepository userRepository,
            AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtService jwtService,
            AuthTokenRepository authTokenRepository,
            AuthenticationService authenticationService,
            RoleRepository roleRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.authTokenRepository = authTokenRepository;
        this.authenticationService = authenticationService;
        this.roleRepository = roleRepository;
    }

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
        customer.addRole(role);
        userRepository.save(customer);

        UserDetailsImpl userDetails = new UserDetailsImpl(customer);
        String token = jwtService.generateToken(userDetails, "activation");
        Claims claims = jwtService.extractAllClaims(token);
        AuthToken authToken = new AuthToken();
        authToken.setUserEmail(customer.getEmail());
        authToken.setToken(token);
        authToken.setTokenType(TokenType.ACTIVATION);
        authToken.setExpirationTime(claims.getExpiration());
        authTokenRepository.save(authToken);


        try {
            emailService.sendActivationLink(customerRegistrationDTO.getEmail(), token);
        } catch (Exception e) {
            throw new Exception("Mail for activating the account is not send");
        }


        return "Customer has been registered";
    }


    public String activateCustomer(@RequestHeader("Authorization") String token) throws Exception {

        try {

            Claims claims = jwtService.extractAllClaims(token);
            String tokenEmail = claims.getSubject();
            User user = userRepository.findByEmail(tokenEmail);
            if (user == null) {
                throw new UserNotFoundException("Invalid Token or User Not found Exception");
            }
            if (user.getIsActive()) {
                throw new AlreadyActivatedException("User is already activated");
            }

            UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
            if (jwtService.isTokenValid(token, userDetailsImpl, "activation")) {
                user.setIsActive(true);
//                user.setLocked(false);
                userRepository.save(user);

                //sends verified user mail
                verifyCustomer(user.getEmail());

                return "User is activated.";
            }else{
                System.out.println(">>>>>>>>token not getting valid>>>>>>>>");
                throw new InvalidTokenException("Invalid activation token.");
            }

        } catch (Exception e) {
            throw new InvalidTokenException("Invalid or expired activation token.");
        }

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

        if (userRepository.findIsActiveByEmail(email)) {
            throw new AlreadyActivatedException("Account is already activated");
        }

        authTokenRepository.deleteTokenByEmail(email);

        User user = userRepository.findByEmail(email);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String token = jwtService.generateToken(userDetails, "activation");

        Claims claims = jwtService.extractAllClaims(token);

        AuthToken authToken = new AuthToken();
        authToken.setUserEmail(email);
        authToken.setToken(token);
        authToken.setTokenType(TokenType.ACTIVATION);
        authToken.setExpirationTime(claims.getExpiration());
        authTokenRepository.save(authToken);

        try {
            emailService.sendActivationLink(email, token);
        } catch (Exception e) {
            throw new Exception("Mail for activating the account is not send");
        }

        return ResponseEntity.ok().body("User is activated");


    }

    @Transactional
    public String refreshToken(String refreshToken) throws RuntimeException {
        AuthToken authToken = authTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new TokenNotFoundException("Refresh token not found"));

        if (authToken.getTokenType() != TokenType.REFRESH) {
            throw new InvalidTokenException("Invalid token type");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        Claims claims = jwtService.extractAllClaims(refreshToken);
        String userEmail = claims.getSubject();
        authTokenRepository.deleteAccessTokenByEmail(userEmail);
        User user = userRepository.findByEmail(userEmail);

        UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
        String newAccessToken = jwtService.generateToken(userDetailsImpl, "access");

        Claims newAccessTokenclaims = jwtService.extractAllClaims(newAccessToken);

        AuthToken accessToken = new AuthToken();
        authToken.setUserEmail(userEmail);
        authToken.setToken(newAccessToken);
        authToken.setTokenType(TokenType.ACCESS);
        authToken.setExpirationTime(newAccessTokenclaims.getExpiration());
        authTokenRepository.save(authToken);

        return newAccessToken;

    }

}