package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.Exception.InvalidTokenOrExpiredException;
import com.shopsavvy.shopshavvy.Exception.UserNotFoundException;
import com.shopsavvy.shopshavvy.dto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.model.users.User;
import com.shopsavvy.shopshavvy.repository.UserRepository;
import com.shopsavvy.shopshavvy.security.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.JwtService;
import com.shopsavvy.shopshavvy.service.SellerAuthenticationService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop-shavvy")
public class SellerController {

    private final SellerAuthenticationService sellerAuthenticationService;

    @Autowired
    public SellerController(SellerAuthenticationService sellerAuthenticationService){
        this.sellerAuthenticationService = sellerAuthenticationService;
    }

    @PostMapping("/seller/signup")
    public ResponseEntity<User> registerSeller(@Valid @RequestBody SellerRegistrationDTO sellerRegistrationDTO) throws Exception{
        User registeredSeller = sellerAuthenticationService.registerSeller(sellerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredSeller);
    }


}
