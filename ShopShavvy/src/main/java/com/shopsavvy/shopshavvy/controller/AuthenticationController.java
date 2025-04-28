package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.customer_dto.CustomerRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.login_dto.LoginRequestDTO;
import com.shopsavvy.shopshavvy.dto.login_dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.password_dto.PasswordDTO;
import com.shopsavvy.shopshavvy.dto.seller_dto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.dto.user_dto.UserRegistrationDTO;
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
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final CustomerAuthenticationService customerAuthenticationService;
    private final SellerAuthenticationService sellerAuthenticationService;

    @PostMapping("/register/customer")
    public ResponseEntity<SuccessMessageResponse<String>> registerCustomer(
            @Valid @RequestBody CustomerRegistrationDTO customerRegistrationDTO) throws Exception {
        String message = customerAuthenticationService.registerCustomer(customerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @PostMapping("/register/seller")
    public ResponseEntity<SuccessMessageResponse<String>> registerSeller(
            @Valid @RequestBody SellerRegistrationDTO sellerRegistrationDTO) throws Exception {
        String message = sellerAuthenticationService.registerSeller(sellerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccessMessageResponse.success(message));
    }

    @PostMapping("/register/admin")
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

    @PostMapping("/resend-activation/customer")
    public ResponseEntity<SuccessMessageResponse<String>> resendActivationLink(
            @Valid @RequestBody EmailDTO emailDTO) throws Exception {
        String message = customerAuthenticationService.resendActivationLink(emailDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PostMapping("/login/customer")
    public ResponseEntity<SuccessMessageResponse<LoginResponseDTO>> authenticateCustomer(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse httpServletResponse) throws MessagingException, BadRequestException {
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(loginRequestDTO, httpServletResponse, "ROLE_CUSTOMER");
        return ResponseEntity.ok(SuccessMessageResponse.success(loginResponseDTO));
    }

    @PostMapping("/login/seller")
    public ResponseEntity<SuccessMessageResponse<LoginResponseDTO>> authenticateSeller(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse httpServletResponse) throws MessagingException, BadRequestException {
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(loginRequestDTO, httpServletResponse, "ROLE_SELLER");
        return ResponseEntity.ok(SuccessMessageResponse.success(loginResponseDTO));
    }

    @PostMapping("/login/admin")
    public ResponseEntity<SuccessMessageResponse<LoginResponseDTO>> authenticateAdmin(
            @Valid @RequestBody LoginRequestDTO loginRequestDTO,
            HttpServletResponse httpServletResponse) throws MessagingException, BadRequestException {
        LoginResponseDTO loginResponseDTO = authenticationService.authenticate(loginRequestDTO, httpServletResponse, "ROLE_ADMIN");
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
