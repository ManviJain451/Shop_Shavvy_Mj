package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.Exception.AlreadyActivatedException;
import com.shopsavvy.shopshavvy.Exception.InvalidTokenException;
import com.shopsavvy.shopshavvy.Exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.UserLoginDTO;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CustomerAuthenticationService;
import com.shopsavvy.shopshavvy.service.JwtService;
import com.shopsavvy.shopshavvy.service.SellerAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop-shavvy/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final CustomerAuthenticationService customerAuthenticationService;
    private final SellerAuthenticationService sellerAuthenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    JwtService jwtService,
                                    CustomerAuthenticationService customerAuthenticationService,
                                    SellerAuthenticationService sellerAuthenticationService){
        this.authenticationService = authenticationService;
        this.jwtService = jwtService;
        this.customerAuthenticationService = customerAuthenticationService;
        this.sellerAuthenticationService = sellerAuthenticationService;
    }

    @PostMapping("/signup/customer")
    public ResponseEntity<String> registerCustomer(@Valid @RequestBody CustomerRegistrationDTO customerRegistrationDTO) throws Exception {
        String message = customerAuthenticationService.registerCustomer(customerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PostMapping("/signup/seller")
    public ResponseEntity<String> registerSeller(@Valid @RequestBody SellerRegistrationDTO sellerRegistrationDTO) throws Exception{
        String message = sellerAuthenticationService.registerSeller(sellerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PutMapping("/activate/customer")
    public ResponseEntity<String> activateCustomer(@RequestHeader("Authorization") String token) throws Exception {
        try {
            String responseMessage = customerAuthenticationService.activateCustomer(token);
            return ResponseEntity.ok(responseMessage);
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (InvalidTokenException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        } catch (AlreadyActivatedException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/resend-ActivationLink/customer")
    public ResponseEntity<String> resendActivationLink(@RequestParam String email) throws Exception {
        return customerAuthenticationService.resendActivationLink(email);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> authenticate(@RequestBody UserLoginDTO userLoginDTO){
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(userLoginDTO);
        return ResponseEntity.ok().body(loginResponseDTO);
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestParam String refreshToken) {
        String newAccessToken = customerAuthenticationService.refreshToken(refreshToken);
        return ResponseEntity.ok().body(newAccessToken);
    }

}
