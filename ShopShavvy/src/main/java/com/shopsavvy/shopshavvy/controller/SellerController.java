package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.AddressUpdateDTO;
import com.shopsavvy.shopshavvy.dto.SellerViewProfileDTO;
import com.shopsavvy.shopshavvy.dto.SellerUpdateProfileDTO;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.SellerService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop-shavvy/seller")
public class SellerController {

    private final AuthenticationService authenticationService;
    private final SellerService sellerService;

    @GetMapping("/hello")
    public String sayHello( String token){
        return "hello seller";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        return authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
    }

    @GetMapping("/view-profile")
    public ResponseEntity<SellerViewProfileDTO> getSellerProfile(@RequestParam String accessToken) {
        SellerViewProfileDTO sellerProfile = sellerService.getSellerProfile(accessToken);
        return ResponseEntity.ok(sellerProfile);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<?> updateSellerProfile(
            @RequestParam String accessToken,
            @ModelAttribute SellerUpdateProfileDTO sellerUpdateProfileDTO) {

        sellerService.updateSellerProfile(accessToken, sellerUpdateProfileDTO);
        return ResponseEntity.ok("Seller profile updated successfully.");
    }

    @PatchMapping("/update-address")
    public ResponseEntity<?> updateAddress(@RequestParam String accessToken, @RequestParam Long addressId,
            @Valid @RequestBody AddressUpdateDTO addressUpdateDTO) {

        sellerService.updateAddress(accessToken, addressId, addressUpdateDTO);
        return ResponseEntity.ok("Address updated successfully.");
    }

}
