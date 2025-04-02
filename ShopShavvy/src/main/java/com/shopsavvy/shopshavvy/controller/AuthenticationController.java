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
    public String activateUser(@RequestHeader("Authorization") String token, @RequestParam("email") String email) {
        System.out.println("🔹 Received activation request");
        System.out.println("🔹 Raw Authorization Header: " + token);

        try {
            // Remove "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            System.out.println("🔹 Extracted Token: " + token);

            Claims claims = jwtService.extractAllClaims(token);
            String tokenEmail = claims.getSubject();
            System.out.println("🔹 Extracted Email from Token: " + tokenEmail);

            if (!tokenEmail.equals(email)) {
                System.out.println("❌ Email mismatch: Token email does not match provided email.");
                return "Email in the token does not match the provided email.";
            }

            User user = userRepository.findByEmail(email);
            if (user == null) {
                System.out.println("❌ User not found for email: " + email);
                return "Invalid activation token.";
            }

            UserDetailsImpl userDetailsImpl = new UserDetailsImpl(user);
            if (jwtService.isTokenValid(token, userDetailsImpl)) {
                System.out.println("Token is valid. Activating user...");
                user.setIsActive(true);
                userRepository.save(user);
                return "Account activated successfully.";
            } else {
                System.out.println("❌ Token is invalid or expired.");
            }

            return "Invalid or expired activation token.";
        } catch (Exception e) {
            System.out.println("❌ Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return "Invalid or expired activation token.";
        }
    }



}
