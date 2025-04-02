package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.CustomerAuthenticationService;
import com.shopsavvy.shopshavvy.service.JwtService;
import com.shopsavvy.shopshavvy.service.SellerAuthenticationService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop-shavvy")
public class AuthenticationController {

    @Autowired
    private UserRepository userRepository;

    private final JwtService jwtService;

    private final CustomerAuthenticationService customerAuthenticationService;
    private final SellerAuthenticationService sellerAuthenticationService;

    public AuthenticationController(JwtService jwtService, CustomerAuthenticationService customerAuthenticationService, SellerAuthenticationService sellerAuthenticationService) {
        this.jwtService = jwtService;
        this.customerAuthenticationService = customerAuthenticationService;
        this.sellerAuthenticationService = sellerAuthenticationService;
    }

    @PostMapping("/customer/signup")
    public ResponseEntity<User> register(@Valid @RequestBody CustomerRegistrationDTO customerRegistrationDTO) throws Exception {
        User registeredCustomer = customerAuthenticationService.signup(customerRegistrationDTO);
        return ResponseEntity.ok(registeredCustomer);
    }

    @PostMapping("/seller/signup")
    public ResponseEntity<User> register(@RequestBody SellerRegistrationDTO sellerRegistrationDTO) throws Exception{
        User registeredSeller = sellerAuthenticationService.signup(sellerRegistrationDTO);
        return ResponseEntity.ok(registeredSeller);
    }


    @GetMapping("/activate")
    public ResponseEntity<User> activateUser(@RequestHeader("Authorization") String token, @RequestParam("email") String email) throws Exception {

        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            Claims claims = jwtService.extractAllClaims(token);
            String tokenEmail = claims.getSubject();

            if (!tokenEmail.equals(email)) {
                throw new Exception("Email in the token does not match the provided email.");
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                throw new Exception("Invalid activation token.");
            }

            UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
            if (jwtService.isTokenValid(token, userDetailsImpl)) {
                user.setIsActive(true);
                userRepository.save(user);
                return ResponseEntity.ok(user);
            }else{
                throw new Exception("Invalid or expired activation token.");
            }


        } catch (Exception e) {
            throw new Exception("Invalid or expired activation token.");
        }
    }



}
