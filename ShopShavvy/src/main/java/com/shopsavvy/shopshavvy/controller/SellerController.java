package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerProfileDTO;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.SellerService;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
    public ResponseEntity<SellerProfileDTO> getSellerProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        SellerProfileDTO sellerProfile = sellerService.getSellerProfile(userDetailsImpl);
        return ResponseEntity.ok(sellerProfile);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<?> updateSellerProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnUpdate.class) @ModelAttribute SellerProfileDTO sellerProfileDTO) {

        sellerService.updateSellerProfile(userDetailsImpl, sellerProfileDTO);
        return ResponseEntity.ok("Seller profile updated successfully.");
    }

    @PatchMapping("/update-address")
    public ResponseEntity<?> updateAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam Long addressId,
            @Validated(OnUpdate.class) @RequestBody AddressDTO addressUpdateDTO) {

        sellerService.updateAddress(userDetailsImpl, addressId, addressUpdateDTO);
        return ResponseEntity.ok("Address updated successfully.");
    }

}
