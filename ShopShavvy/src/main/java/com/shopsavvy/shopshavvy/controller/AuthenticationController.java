package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.exception.AlreadyActivatedException;
import com.shopsavvy.shopshavvy.exception.InvalidTokenException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.dto.*;

import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CustomerAuthenticationService;
import com.shopsavvy.shopshavvy.service.SellerAuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop-shavvy/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final CustomerAuthenticationService customerAuthenticationService;
    private final SellerAuthenticationService sellerAuthenticationService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService,
                                    CustomerAuthenticationService customerAuthenticationService,
                                    SellerAuthenticationService sellerAuthenticationService){
        this.authenticationService = authenticationService;
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
    public ResponseEntity<String> activateCustomer(@RequestParam String token) throws Exception {
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
    public ResponseEntity<String> resendActivationLink(@Valid @RequestParam String email) throws Exception {
        return customerAuthenticationService.resendActivationLink(email);
    }

    @PostMapping("/customer/login")
    public ResponseEntity<LoginResponseDTO> authenticateCustomer(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletResponse httpServletResponse){
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(userLoginDTO, httpServletResponse);
        return ResponseEntity.ok().body(loginResponseDTO);
    }

    @PostMapping("/seller/login")
    public ResponseEntity<LoginResponseDTO> authenticateSeller(@Valid @RequestBody UserLoginDTO userLoginDTO, HttpServletResponse httpServletResponse){
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(userLoginDTO, httpServletResponse);
        return ResponseEntity.ok().body(loginResponseDTO);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestParam String refreshToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        String newAccessToken = customerAuthenticationService.refreshToken(refreshToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok().body(newAccessToken);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestParam String email) throws MessagingException {
        return authenticationService.forgotPassword(email);
    }

    @PutMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponseDTO> resetPassword(@RequestParam String resetPasswordtoken, @Valid @RequestParam String password, @RequestParam String confirmPassword) throws MessagingException {
        return authenticationService.resetPassword(resetPasswordtoken, password, confirmPassword);
    }

}
