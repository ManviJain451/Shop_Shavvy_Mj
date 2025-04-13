package com.shopsavvy.shopshavvy.controller;


import com.shopsavvy.shopshavvy.dto.CustomerResponseDTO;
import com.shopsavvy.shopshavvy.dto.SellerResponseDTO;
import com.shopsavvy.shopshavvy.service.AdminService;
import com.shopsavvy.shopshavvy.service.AuthenticationService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<String> unlockUser(@Valid @RequestParam String email){
        String message = adminService.unlockUser(email);
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
    public ResponseEntity<String> activateCustomer(@Valid @RequestParam String customerID) throws MessagingException {
        String responseMessage = adminService.activateCustomer(customerID);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/activate-seller")
    public ResponseEntity<String> activateSeller(@Valid @RequestParam String sellerID) throws MessagingException {
        String responseMessage = adminService.activateSeller(sellerID);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/deactivate-customer")
    public ResponseEntity<String> deactivateCustomer(@Valid @RequestParam String customerID) {
        String responseMessage = adminService.deactivateCustomer(customerID);
        return ResponseEntity.ok(responseMessage);
    }

    @PutMapping("/deactivate-seller")
    public ResponseEntity<String> deactivateSeller(@Valid @RequestParam String sellerID) {
        String responseMessage = adminService.deactivateSeller(sellerID);
        return ResponseEntity.ok(responseMessage);
    }


}
