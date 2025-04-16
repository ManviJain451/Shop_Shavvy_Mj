package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerProfileDTO;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.SellerService;
import com.shopsavvy.shopshavvy.utilities.SuccessMessageResponse;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

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
    public ResponseEntity<SuccessMessageResponse<String>> logout(@RequestParam String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        String message = authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @GetMapping("/view-profile")
    public ResponseEntity<SuccessMessageResponse<SellerProfileDTO>> getSellerProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        SellerProfileDTO sellerProfile = sellerService.getSellerProfile(userDetailsImpl);
        return ResponseEntity.ok(SuccessMessageResponse.success(sellerProfile));
    }

    @PutMapping("/update-profile")
    public ResponseEntity<SuccessMessageResponse<String>> updateSellerProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnUpdate.class) @ModelAttribute SellerProfileDTO sellerProfileDTO) {

        String message = sellerService.updateSellerProfile(userDetailsImpl, sellerProfileDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

    @PutMapping("/update-address")
    public ResponseEntity<SuccessMessageResponse<String>> updateAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, @RequestParam String addressId,
            @Validated(OnUpdate.class) @RequestBody AddressDTO addressUpdateDTO) throws BadRequestException {

        String message = sellerService.updateAddress(userDetailsImpl, addressId, addressUpdateDTO);
        return ResponseEntity.ok(SuccessMessageResponse.success(message));
    }

}
