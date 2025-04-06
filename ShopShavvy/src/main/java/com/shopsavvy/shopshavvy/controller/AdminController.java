package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shop-shavvy/admin")
public class AdminController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AdminController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(String accessToken){
        return authenticationService.userLogout(accessToken);
    }
}
