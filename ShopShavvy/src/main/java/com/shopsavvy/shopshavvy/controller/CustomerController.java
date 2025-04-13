package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.*;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import com.shopsavvy.shopshavvy.service.CustomerService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<CustomerViewProfileDTO> getProfile(@RequestParam String accessToken) {
        CustomerViewProfileDTO customerProfile = customerService.getCustomerProfile(accessToken);
        return ResponseEntity.ok(customerProfile);
    }

    @GetMapping("/view-addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(@RequestParam String accessToken) {
        List<AddressDTO> addresses = customerService.getCustomerAddresses(accessToken);
        return ResponseEntity.ok(addresses);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestParam String accessToken,
            @Valid @ModelAttribute CustomerUpdateProfileDTO customerUpdateProfileDTO) {

        customerService.updateCustomerProfile(accessToken, customerUpdateProfileDTO);
        return ResponseEntity.ok("Profile updated successfully.");
    }

    @PostMapping("/add-address")
    public ResponseEntity<?> addAddress(
            @RequestParam String accessToken,
            @Valid @ModelAttribute AddressDTO addressDTO) {

        customerService.addCustomerAddress(accessToken, addressDTO);
        return ResponseEntity.ok("Address added successfully.");
    }

    @DeleteMapping("/delete-address")
    public ResponseEntity<String> deleteAddress(
            @RequestParam String accessToken,
            @RequestParam String addressId) {

        customerService.deleteCustomerAddress(accessToken, addressId);
        return ResponseEntity.ok("Address deleted successfully.");
    }

    @PatchMapping("/update-address")
    public ResponseEntity<String> updateCustomerAddress(@RequestParam String accessToken,
            @RequestParam String addressId,
            @Valid @ModelAttribute CustomerAddressDTO customerAddressDTO) {

        customerService.updateCustomerAddress(accessToken, addressId, customerAddressDTO);
        return ResponseEntity.ok("Address updated successfully.");
    }
}
