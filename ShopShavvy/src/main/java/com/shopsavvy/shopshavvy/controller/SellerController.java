package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop-shavvy/seller")
public class SellerController {

    private final AuthenticationService authenticationService;

    @Autowired
    public SellerController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @GetMapping("/hello")
    public String sayHello( String token){
        return "hello";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(String accessToken){
        return authenticationService.userLogout(accessToken);
    }


}
