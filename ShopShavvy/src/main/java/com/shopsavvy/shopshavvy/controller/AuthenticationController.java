package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.loginDto.LoginRequestDTO;
import com.shopsavvy.shopshavvy.dto.loginDto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.passwordDto.PasswordDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.userDto.UserRegistrationDTO;
import com.shopsavvy.shopshavvy.exception.AlreadyActivatedException;
import com.shopsavvy.shopshavvy.exception.InvalidTokenException;
import com.shopsavvy.shopshavvy.exception.UserNotFoundException;

import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CustomerAuthenticationService;
import com.shopsavvy.shopshavvy.service.SellerAuthenticationService;
import com.shopsavvy.shopshavvy.utilities.SuccessMessageResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/shop-shavvy/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final CustomerAuthenticationService customerAuthenticationService;
    private final SellerAuthenticationService sellerAuthenticationService;

    @PostMapping("/signup/customer")
    public ResponseEntity<SuccessMessageResponse<String>> registerCustomer(
            @Valid @RequestBody CustomerRegistrationDTO customerRegistrationDTO) throws Exception {
        String message = customerAuthenticationService.registerCustomer(customerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @PostMapping("/signup/seller")
    public ResponseEntity<SuccessMessageResponse<String>> registerSeller(
            @Valid @RequestBody SellerRegistrationDTO sellerRegistrationDTO) throws Exception {
        String message = sellerAuthenticationService.registerSeller(sellerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @PostMapping("/signup/admin")
    public ResponseEntity<SuccessMessageResponse<String>> registerAdmin(
            @Valid @RequestBody UserRegistrationDTO userRegistrationDTO) throws Exception {
        String message = authenticationService.registerAdmin(userRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @PutMapping("/activate/customer")
    public ResponseEntity<?> activateCustomer(@RequestParam String token) throws Exception {
        try {
            String responseMessage = customerAuthenticationService.activateCustomer(token);
            return ResponseEntity.ok(SuccessMessageResponse.success(responseMessage));
        } catch (UserNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        } catch (InvalidTokenException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        } catch (AlreadyActivatedException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PostMapping("/resend-ActivationLink/customer")
    public ResponseEntity<SuccessMessageResponse<String>> resendActivationLink(
            @Valid @RequestBody EmailDTO emailDTO) throws Exception {
        String message = customerAuthenticationService.resendActivationLink(emailDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/customer/login")
    public ResponseEntity<SuccessMessageResponse<LoginResponseDTO>> authenticateCustomer(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse httpServletResponse) throws MessagingException {
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(loginRequestDTO, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(loginResponseDTO));
    }

    @PostMapping("/seller/login")
    public ResponseEntity<SuccessMessageResponse<LoginResponseDTO>> authenticateSeller(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse httpServletResponse) throws MessagingException {
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(loginRequestDTO, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(loginResponseDTO));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<SuccessMessageResponse<LoginResponseDTO>> authenticateAdmin(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse httpServletResponse) throws MessagingException {
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(loginRequestDTO, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(loginResponseDTO));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<SuccessMessageResponse<String>> refreshToken(
            @RequestParam String refreshToken,
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse) throws MessagingException {
        String newAccessToken = authenticationService.refreshToken(refreshToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(newAccessToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<SuccessMessageResponse<String>> forgotPassword(
            @Valid @RequestBody EmailDTO emailDTO) throws MessagingException {
        String message = authenticationService.forgotPassword(emailDTO.getEmail());
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/reset-password")
    public ResponseEntity<SuccessMessageResponse<String>> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody PasswordDTO passwordDTO) throws MessagingException {
        String message = authenticationService.resetPassword(token, passwordDTO.getPassword(), passwordDTO.getConfirmPassword());
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }
}
