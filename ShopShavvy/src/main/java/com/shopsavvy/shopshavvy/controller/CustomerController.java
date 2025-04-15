package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.addressDto.CustomerAddressDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerProfileDTO;
import com.shopsavvy.shopshavvy.security.configurations.UserDetailsImpl;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CustomerService;
import com.shopsavvy.shopshavvy.validation.groups.OnCreate;
import com.shopsavvy.shopshavvy.validation.groups.OnUpdate;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<CustomerProfileDTO> getProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        CustomerProfileDTO customerProfileDTO = customerService.getCustomerProfile(userDetailsImpl);
        return ResponseEntity.ok(customerProfileDTO);
    }

    @GetMapping("/view-addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl) {
        List<AddressDTO> addresses = customerService.getCustomerAddresses(userDetailsImpl);
        return ResponseEntity.ok(addresses);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnUpdate.class) @ModelAttribute CustomerProfileDTO customerProfileDTO) {

        customerService.updateCustomerProfile(userDetailsImpl, customerProfileDTO);
        return ResponseEntity.ok("Profile updated successfully.");
    }

    @PostMapping("/add-address")
    public ResponseEntity<?> addAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @Validated(OnCreate.class) @RequestBody CustomerAddressDTO customerAddressDTO) {

        customerService.addCustomerAddress(userDetailsImpl, customerAddressDTO);
        return ResponseEntity.ok("Address added successfully.");
    }

    @DeleteMapping("/delete-address")
    public ResponseEntity<String> deleteAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @RequestParam String addressId) {

        customerService.deleteCustomerAddress(userDetailsImpl, addressId);
        return ResponseEntity.ok("Address deleted successfully.");
    }

    @PatchMapping("/update-address")
    public ResponseEntity<String> updateCustomerAddress(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            @RequestParam String addressId,
            @Validated(OnUpdate.class) @RequestBody CustomerAddressDTO customerAddressDTO) {

        customerService.updateCustomerAddress(userDetailsImpl, addressId, customerAddressDTO);
        return ResponseEntity.ok("Address updated successfully.");
    }
}
