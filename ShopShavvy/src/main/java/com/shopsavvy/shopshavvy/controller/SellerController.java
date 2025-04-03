package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.SellerRegistrationDTO;
import com.shopsavvy.shopshavvy.service.SellerAuthenticationService;
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
    public ResponseEntity<String> registerSeller(@Valid @RequestBody SellerRegistrationDTO sellerRegistrationDTO) throws Exception{
        String message = sellerAuthenticationService.registerSeller(sellerRegistrationDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }


}
