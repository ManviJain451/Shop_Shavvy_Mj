package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.LoginResponseDTO;
import com.shopsavvy.shopshavvy.dto.UserLoginDTO;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop-shavvy/customer")
public class CustomerController {

    private final AuthenticationService authenticationService;

    @Autowired
    public CustomerController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @GetMapping("/hello")
    public String sayHello(String token){
        return "hello customer";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam  String accessToken){
        return authenticationService.userLogout(accessToken);
    }
}
