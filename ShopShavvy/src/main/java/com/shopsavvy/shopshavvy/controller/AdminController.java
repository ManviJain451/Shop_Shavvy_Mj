package com.shopsavvy.shopshavvy.controller;


import com.shopsavvy.shopshavvy.dto.EmailDTO;
import com.shopsavvy.shopshavvy.dto.customerDto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.sellerDto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.service.AdminService;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
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
@RequestMapping("/shop-shavvy/admin")
public class AdminController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;

    @GetMapping("/hello")
    public String sayHello(String token){
        return "hello admin";
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestParam String accessToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws MessagingException {
        return authenticationService.userLogout(accessToken, httpServletRequest, httpServletResponse);
    }

    @PutMapping("/unlock/user")
    public ResponseEntity<String> unlockUser(@Valid @RequestParam EmailDTO emailDTO){
        String message = adminService.unlockUser(emailDTO);
        return ResponseEntity.ok().body(message);
    }

    @GetMapping("/registered-customers")
    public ResponseEntity<List<CustomerResponseDTO>> getAllCustomers(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "dateCreated") String sort,
            @RequestParam(required = false) String email) {
        List<CustomerResponseDTO> customers = adminService.getAllCustomers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/registered-sellers")
    public ResponseEntity<List<SellerResponseDTO>> getAllSellers(
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int pageOffset,
            @RequestParam(defaultValue = "dateCreated") String sort,
            @RequestParam(required = false) String email) {
        List<SellerResponseDTO> sellers = adminService.getAllSellers(pageSize, pageOffset, sort, email);
        return ResponseEntity.ok(sellers);
    }

    @PutMapping("/activate-customer")
    public ResponseEntity<String> activateCustomer(@RequestParam String customerID) throws MessagingException {
        String responseMessage = adminService.activateCustomer(customerID);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/activate-seller")
    public ResponseEntity<String> activateSeller(@RequestParam String sellerID) throws MessagingException {
        String responseMessage = adminService.activateSeller(sellerID);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/deactivate-customer")
    public ResponseEntity<String> deactivateCustomer(@RequestParam String customerID) {
        String responseMessage = adminService.deactivateCustomer(customerID);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/deactivate-seller")
    public ResponseEntity<String> deactivateSeller(@RequestParam String sellerID) {
        String responseMessage = adminService.deactivateSeller(sellerID);
        return ResponseEntity.ok(responseMessage);
    }


}
