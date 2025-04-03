package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.Exception.AlreadyActivatedException;
import com.shopsavvy.shopshavvy.Exception.InvalidTokenOrExpiredException;
import com.shopsavvy.shopshavvy.Exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.AuthToken;
import com.shopsavvy.shopshavvy.model.users.TokenType;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.AuthTokenRepository;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CustomerAuthenticationService;
import com.shopsavvy.shopshavvy.service.EmailService;
import com.shopsavvy.shopshavvy.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop-shavvy")
public class CustomerController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuthenticationService authenticationService;
    private final CustomerAuthenticationService customerAuthenticationService;
    private final AuthTokenRepository authTokenRepository;
    private final EmailService emailService;

    @Autowired
    public CustomerController(JwtService jwtService,
                              UserRepository userRepository,
                              AuthenticationService authenticationService,
                              CustomerAuthenticationService customerAuthenticationService,
                              AuthTokenRepository authTokenRepository,
                              EmailService emailService){
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.customerAuthenticationService = customerAuthenticationService;
        this.authTokenRepository = authTokenRepository;
        this.emailService = emailService;
    }

    @PostMapping("/customer/signup")
    public ResponseEntity<User> registerCustomer(@Valid @RequestBody CustomerRegistrationDTO customerRegistrationDTO) throws Exception {
        User registeredCustomer = customerAuthenticationService.registerCustomer(customerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredCustomer);
    }

    @PutMapping("/customer/activate")
    public ResponseEntity<String> activateCustomer(@RequestHeader("Authorization") String token) throws Exception {
        try {
            String responseMessage = customerAuthenticationService.activateCustomer(token);
            return ResponseEntity.ok(responseMessage);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (InvalidTokenOrExpiredException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        } catch (AlreadyActivatedException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/customer/resend-ActivationLink")
    public ResponseEntity<String> resendActivationLink(@RequestParam String email) throws Exception {
        return customerAuthenticationService.resendActivationLink(email);
    }
}
