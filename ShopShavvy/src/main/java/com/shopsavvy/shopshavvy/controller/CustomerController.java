package com.shopsavvy.shopshavvy.controller;

import com.shopsavvy.shopshavvy.dto.addressDto.AddressDTO;
import com.shopsavvy.shopshavvy.dto.addressDto.CustomerAddressDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerProfileDTO;
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
    public ResponseEntity<CustomerProfileDTO> getProfile(@RequestParam String accessToken) {
        CustomerProfileDTO customerProfileDTO = customerService.getCustomerProfile(accessToken);
        return ResponseEntity.ok(customerProfileDTO);
    }

    @GetMapping("/view-addresses")
    public ResponseEntity<List<AddressDTO>> getAddresses(@RequestParam String accessToken) {
        List<AddressDTO> addresses = customerService.getCustomerAddresses(accessToken);
        return ResponseEntity.ok(addresses);
    }

    @PatchMapping("/update-profile")
    public ResponseEntity<String> updateProfile(@RequestParam String accessToken,
            @Validated(OnUpdate.class) @ModelAttribute CustomerProfileDTO customerProfileDTO) {

        customerService.updateCustomerProfile(accessToken, customerProfileDTO);
        return ResponseEntity.ok("Profile updated successfully.");
    }

    @PostMapping("/add-address")
    public ResponseEntity<?> addAddress(@RequestParam String accessToken,
            @Validated(OnCreate.class) @RequestBody CustomerAddressDTO customerAddressDTO) {

        customerService.addCustomerAddress(accessToken, customerAddressDTO);
        return ResponseEntity.ok("Address added successfully.");
    }

    @DeleteMapping("/delete-address")
    public ResponseEntity<String> deleteAddress(@RequestParam String accessToken,
            @RequestParam String addressId) {

        customerService.deleteCustomerAddress(accessToken, addressId);
        return ResponseEntity.ok("Address deleted successfully.");
    }

    @PatchMapping("/update-address")
    public ResponseEntity<String> updateCustomerAddress(@RequestParam String accessToken,
            @RequestParam String addressId,
            @Validated(OnUpdate.class) @RequestBody CustomerAddressDTO customerAddressDTO) {

        customerService.updateCustomerAddress(accessToken, addressId, customerAddressDTO);
        return ResponseEntity.ok("Address updated successfully.");
    }
}
