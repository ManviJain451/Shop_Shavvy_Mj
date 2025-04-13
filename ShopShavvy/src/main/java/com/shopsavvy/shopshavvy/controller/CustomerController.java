package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.CustomerViewProfileDTO;
import com.shopsavvy.shopshavvy.dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.UserLoginDTO;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CustomerService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop-shavvy/customer")
public class CustomerController {

    private final AuthenticationService authenticationService;
    private final CustomerService customerService;

    @GetMapping("/hello")
    public String sayHello(String token){
        return "hello customer";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam  String accessToken,
                                         HttpServletRequest httpServletRequest,
                                         HttpServletResponse httpServletResponse) throws MessagingException {
        return authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
    }

    @GetMapping("/view-profile")
    public ResponseEntity<CustomerViewProfileDTO> getProfile(@RequestParam String accessToken) {
        CustomerViewProfileDTO customerProfile = customerService.getCustomerProfile(accessToken);
        return ResponseEntity.ok(customerProfile);
    }
}
